package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

public class AccountManager implements ClientAccount {
	private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
	private static final AtomicLong NULL = new AtomicLong(0);
	private final AtomicLong tradeIdGenerator = new AtomicLong(0);

	final Map<Integer, long[]> fundAllocationCache = new ConcurrentHashMap<>();
	final Map<String, AtomicLong> balanceUpdateCounts = new ConcurrentHashMap<>();

	final Map<String, Trader> traders = new ConcurrentHashMap<>();
	final AccountConfiguration<?> configuration;

	final OrderSet pendingOrders = new OrderSet();
	private final OrderSet orderUpdates = new OrderSet();

	private static final long BALANCE_EXPIRATION_TIME = minutes(10).ms;
	private static final long FREQUENT_BALANCE_UPDATE_INTERVAL = seconds(15).ms;

	private long lastBalanceSync = 0L;
	final ConcurrentHashMap<String, Balance> balances = new ConcurrentHashMap<>();
	Balance[] balancesArray;
	private final Lock balanceLock;

	final ExchangeClient client;
	private final ClientAccount account;
	final Map<String, TradingManager> allTradingManagers = new ConcurrentHashMap<>();
	TradingManager[] tradingManagers;
	final double marginReserveFactor;
	private final double marginReserveFactorPct;
	private final int accountHash;

	public AccountManager(ClientAccount account, AccountConfiguration<?> configuration) {
		if (StringUtils.isBlank(configuration.referenceCurrency())) {
			throw new IllegalConfigurationException("Please configure the reference currency symbol");
		}
		if (configuration.symbolPairs().isEmpty()) {
			throw new IllegalConfigurationException("Please configure traded symbol pairs");
		}
		this.accountHash = configuration.id().hashCode();
		this.account = account;
		this.configuration = configuration;

		this.marginReserveFactor = account.marginReservePercentage() / 100.0;
		this.marginReserveFactorPct = marginReserveFactor;

		this.client = new ExchangeClient(this);

		this.balanceLock = account.isSimulated() ? new FakeLock() : new ReentrantLock();

		if (account.marginReservePercentage() < 100) {
			throw new IllegalStateException("Margin reserve percentage must be at least 100%");
		}
	}

	public ExchangeClient getClient() {
		return client;
	}

	ConcurrentHashMap<String, Balance> getBalances() {
		updateBalances();
		return balances;
	}


	/**
	 * Returns the amount held in the account for the given symbol.
	 *
	 * @param symbol the symbol whose amount will be returned
	 *
	 * @return the amount held for the given symbol.
	 */
	public double getAmount(String symbol) {
		return getBalance(symbol, Balance::getFree);
	}

	public double getShortedAmount(String symbol) {
		return getBalance(symbol, Balance::getShorted);
	}

	Balance getBalance(String symbol) {
		Balance out = balances.get(symbol);
		if (out == null) {
			out = new Balance(this, symbol);
			balances.put(symbol, out);
			balancesArray = null;
		}
		return out;
	}

	public final void consumeBalance(String symbol, Consumer<Balance> consumer) {
		modifyBalance(symbol, consumer);
	}

	public final void modifyBalance(String symbol, Consumer<Balance> consumer) {
		balanceLock.lock();
		try {
			consumer.accept(getBalance(symbol));
		} finally {
			balanceLock.unlock();
		}
	}

	public final <T> T queryBalance(String symbol, Function<Balance, T> function) {
		balanceLock.lock();
		try {
			return function.apply(getBalance(symbol));
		} finally {
			balanceLock.unlock();
		}
	}

	public final double getBalance(String symbol, ToDoubleFunction<Balance> function) {
		balanceLock.lock();
		try {
			return function.applyAsDouble(getBalance(symbol));
		} finally {
			balanceLock.unlock();
		}
	}

	private Trader findTrader(String assetSymbol, String fundSymbol) {
		Trader trader = getTraderOf(assetSymbol + fundSymbol);
		if (trader != null) {
			return trader;
		}

		for (String[] pair : getTradedPairs()) {
			if (assetSymbol.equals(pair[0])) {
				trader = getTraderOf(pair[0] + pair[1]);
				if (trader != null) {
					return trader;
				}
			}
		}
		return null;
	}

	private double executeAllocateFunds(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
		TradingManager tradingManager = getTradingManagerOf(assetSymbol + fundSymbol);
		if (tradingManager == null) {
			Trader trader = getTraderOf(assetSymbol + configuration.referenceCurrency());
			if (trader == null) {
				trader = findTrader(assetSymbol, fundSymbol);
			}
			if (trader != null) {
				tradingManager = trader.tradingManager;
				fundSymbol = tradingManager.getFundSymbol();
			} else {
				throw new IllegalStateException("Unable to allocate funds to buy " + assetSymbol + ". Unknown symbol: " + assetSymbol + fundSymbol + ". Trading with " + getTradedSymbols());
			}
		}

		double minimumInvestment = configuration.minimumInvestmentAmountPerTrade(assetSymbol);
		double percentage = configuration.maximumInvestmentPercentagePerAsset(assetSymbol) / 100.0;
		double maxAmount = configuration.maximumInvestmentAmountPerAsset(assetSymbol);
		double percentagePerTrade = configuration.maximumInvestmentPercentagePerTrade(assetSymbol) / 100.0;
		double maxAmountPerTrade = configuration.maximumInvestmentAmountPerTrade(assetSymbol);

		if (percentage == 0.0 || maxAmount == 0.0) {
			return 0.0;
		}

		double totalFunds = getTotalFundsIn(fundSymbol);
		maxAmountPerTrade = Math.min(totalFunds * percentagePerTrade, maxAmountPerTrade);

		double allocated = getAmount(assetSymbol);
		double shorted = getShortedAmount(assetSymbol);
		double unitPrice = tradingManager.getLatestPrice();
		allocated = allocated * unitPrice;
		shorted = shorted * unitPrice;

		double available = (totalFunds - allocated) - shorted;
		double allocation = totalFunds * percentage;
		if (allocation > maxAmount) {
			allocation = maxAmount;
		}
		double max = allocation - allocated;
		if (available > max) {
			available = max;
		}
		available = Math.min(maxAmountPerTrade, Math.min(maxAmount, available));

		final double freeAmount = getAmount(fundSymbol);
		double out = Math.min(available, freeAmount);

		if (tradeSide == SHORT) {
			out *= marginReserveFactorPct;
			out = Math.min(out, freeAmount);
			out = Math.min(out, maxAmountPerTrade);
		}
		if (out < minimumInvestment) {
			return 0.0;
		}
		return out;
	}

	private int hash(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
		int result = 31 + accountHash;
		result = 31 * result + assetSymbol.hashCode();
		result = 31 * result + fundSymbol.hashCode();
		result = 31 * result + tradeSide.hashCode();
		return result;
	}

	public final double allocateFunds(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
		long a = balanceUpdateCounts.getOrDefault(assetSymbol, NULL).get();
		long f = balanceUpdateCounts.getOrDefault(fundSymbol, NULL).get();
		Integer hash = hash(assetSymbol, fundSymbol, tradeSide);
		long[] cached = fundAllocationCache.get(hash);
		if (cached == null) {
			double funds = executeAllocateFunds(assetSymbol, fundSymbol, tradeSide);
			if (a > 0 && f > 0) {
				cached = new long[3];
				cached[0] = a;
				cached[1] = f;
				cached[2] = Double.doubleToLongBits(funds);
				fundAllocationCache.put(hash, cached);
			}
			return funds;
		} else if (cached[0] != a || cached[1] != f) {
			cached[0] = a;
			cached[1] = f;
			double funds = executeAllocateFunds(assetSymbol, fundSymbol, tradeSide);
			cached[2] = Double.doubleToLongBits(funds);
			fundAllocationCache.put(hash, cached);
			return funds;
		} else {
			return Double.longBitsToDouble(cached[2]);
		}
	}

	public final double allocateFunds(String assetSymbol, Trade.Side tradeSide) {
		return allocateFunds(assetSymbol, getReferenceCurrencySymbol(), tradeSide);
	}

	public double getTotalFundsInReferenceCurrency() {
		return getTotalFundsIn(configuration.referenceCurrency());
	}

	public double getTotalFundsIn(String currency) {
		final Balance[] tmp;
		balanceLock.lock();
		double total = 0.0;
		try {
			if (balancesArray == null) {
				tmp = balancesArray = balances.values().toArray(new Balance[0]);
			} else {
				tmp = balancesArray;
			}
			Map<String, double[]> allPrices = getAllTradingManagers()[0].getAllPrices();
			for (int i = 0; i < tmp.length; i++) {
				Balance b = tmp[i];
				String symbol = b.getSymbol();
				double quantity = b.getTotal();

				String[] shortedAssetSymbols = b.getShortedAssetSymbols();
				for (int j = 0; j < shortedAssetSymbols.length; j++) {
					String shorted = shortedAssetSymbols[j];
					double reserve = b.getMarginReserve(shorted);
					double marginWithoutReserve = reserve / marginReserveFactorPct;
					double accountBalanceForMargin = reserve - marginWithoutReserve;
					double shortedQuantity = getBalance(shorted, Balance::getShorted);
					double originalShortedPrice = marginWithoutReserve / shortedQuantity;
					double totalInvestmentOnShort = shortedQuantity * originalShortedPrice;
					double totalAtCurrentPrice = multiplyWithLatestPrice(shortedQuantity, shorted, symbol, allPrices);
					double shortProfitLoss = totalInvestmentOnShort - totalAtCurrentPrice;

					total += accountBalanceForMargin + shortProfitLoss;
				}

				if (currency.equals(symbol)) {
					total += quantity;
				} else {
					total += multiplyWithLatestPrice(quantity, symbol, currency, allPrices);
				}
			}
		} finally {
			balanceLock.unlock();
		}
		return total;
	}

	private static final double[] DEFAULT = new double[]{-1.0};

	private double multiplyWithLatestPrice(double quantity, String symbol, String currency, Map<String, double[]> allPrices) {
		double price = allPrices.getOrDefault(symbol + currency, DEFAULT)[0];
		if (price > 0.0) {
			return quantity * price;
		} else {
			price = allPrices.getOrDefault(currency + symbol, DEFAULT)[0];
			if (price > 0.0) {
				return quantity / price;
			}
		}
		return 0.0;
	}

	public boolean waitingForFill(String assetSymbol, Order.Side side, Trade.Side tradeSide) {
		for (int i = pendingOrders.i - 1; i >= 0; i--) {
			Order order = pendingOrders.elements[i];

			if (order.isFinalized() || order.getTradeSide() != tradeSide) {
				continue;
			}

			if (order.getSide() == side && order.getAssetsSymbol().equals(assetSymbol)) {
				return true;
			}
			// If we want to know if there is an open order to buy BTC,
			// and the symbol is "ADABTC", we need to invert the side as
			// we are selling ADA to buy BTC.
			if (side == BUY && order.isSell() && order.getFundsSymbol().equals(assetSymbol)) {
				return true;

			} else if (side == SELL && order.isBuy() && order.getFundsSymbol().equals(assetSymbol)) {
				return true;
			}

		}
		return false;
	}

	private boolean isTradingLocked(String assetSymbol) {
		return queryBalance(assetSymbol, Balance::isTradingLocked);
	}

	public boolean isBuyLocked(String assetSymbol) {
		return isTradingLocked(assetSymbol) || waitingForFill(assetSymbol, BUY, LONG);
	}

	public boolean isShortSellLocked(String assetSymbol) {
		return (isTradingLocked(assetSymbol) || waitingForFill(assetSymbol, SELL, SHORT));
	}

	private boolean lockTrading(String assetSymbol) {
		return queryBalance(assetSymbol, b -> {
			if (b.isTradingLocked()) {
				return false;
			}
			b.lockTrading();
			return true;
		});
	}

	private void unlockTrading(String assetSymbol) {
		modifyBalance(assetSymbol, Balance::unlockTrading);
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		Map<String, Balance> positions = account.updateBalances();
		positions.entrySet().stream()
				.filter((e) -> e.getValue().getTotal() > 0.00001)
				.forEach((e) -> out
						.append(e.getKey())
						.append(" = $")
						.append(SymbolPriceDetails.toBigDecimal(2, e.getValue().getTotal()).toPlainString())
						.append('\n'));

		return out.toString();
	}

	private void executeUpdateBalances() {
		if (balanceLock.tryLock()) {
			try {
				if (System.currentTimeMillis() - lastBalanceSync < FREQUENT_BALANCE_UPDATE_INTERVAL) {
					return;
				}
				Map<String, Balance> updatedBalances = account.updateBalances();
				if (updatedBalances != null && updatedBalances != balances && !updatedBalances.isEmpty()) {
					log.trace("Balances updated - available: " + new TreeMap<>(updatedBalances).values());
					updatedBalances.keySet().retainAll(configuration.symbols());

					this.balances.clear();
					this.balances.putAll(updatedBalances);
					this.balancesArray = null;


					updatedBalances.values().removeIf(b -> b.getTotal() == 0);
					log.debug("Balances updated - trading: " + new TreeMap<>(updatedBalances).values());
				}
				lastBalanceSync = System.currentTimeMillis();
			} finally {
				balanceLock.unlock();
			}
		}
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances() {
		if (System.currentTimeMillis() - lastBalanceSync < FREQUENT_BALANCE_UPDATE_INTERVAL) {
			return balances;
		}
		executeUpdateBalances();

		return balances;
	}

	public String getReferenceCurrencySymbol() {
		return configuration.referenceCurrency();
	}

	public Order buy(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity, Trade trade) {
		if (lockTrading(assetSymbol)) {
			try {
				String symbol = assetSymbol + fundSymbol;
				TradingManager tradingManager = getTradingManagerOf(symbol);
				if (tradingManager == null) {
					throw new IllegalStateException("Unable to buy " + quantity + " units of unknown symbol: " + symbol);
				}
				if (tradeSide == SHORT) {
					OrderRequest orderPreparation = prepareOrder(tradingManager, BUY, SHORT, quantity, null, trade);
					return executeOrder(orderPreparation);
				}
				double maxSpend = allocateFunds(assetSymbol, tradeSide);
				if (maxSpend > 0) {
					maxSpend = getTradingFees().takeFee(maxSpend, Order.Type.MARKET, BUY);
					double expectedCost = quantity * tradingManager.getLatestPrice();
					if (expectedCost > maxSpend) {
						quantity = quantity * (maxSpend / expectedCost);
					}
					quantity = quantity * 0.9999;
					OrderRequest orderPreparation = prepareOrder(tradingManager, BUY, tradeSide, quantity, null, trade);
					return executeOrder(orderPreparation);
				}
			} finally {
				unlockTrading(assetSymbol);
			}
		}
		return null;
	}

	public Order sell(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity, Trade trade) {
		String symbol = assetSymbol + fundSymbol;
		TradingManager tradingManager = getTradingManagerOf(symbol);
		if (tradingManager == null) {
			throw new IllegalStateException("Unable to sell " + quantity + " units of unknown symbol: " + symbol);
		}
		OrderRequest orderPreparation = prepareOrder(tradingManager, SELL, tradeSide, quantity, null, trade);
		return executeOrder(orderPreparation);
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		if (orderDetails != null) {
			if (orderDetails.isCancelled()) {
				return null;
			}
			if (orderDetails.getQuantity() == 0) {
				throw new IllegalArgumentException("No quantity specified for order " + orderDetails);
			}
			if (orderDetails.getPrice() == 0 && orderDetails.getType() == Order.Type.LIMIT) {
				throw new IllegalArgumentException("No price specified for LIMIT order " + orderDetails);
			}

			Order order = account.executeOrder(orderDetails);
			if (order != null && order.getStatus() == CANCELLED) {
				logOrderStatus("Could not create order. ", order);
				orderFinalized(null, order);
				return null;
			} else {
				return order;
			}
		}
		return null;
	}

	void initiateOrderMonitoring(Order order) {
		if (order != null) {
			if (order.getTrade() == null) {
				throw new IllegalStateException("Order " + order + " does not have a valid trade associated with it.");
			}
			switch (order.getStatus()) {
				case NEW:
				case PARTIALLY_FILLED:
					logOrderStatus("Tracking pending order. ", order);
					waitForFill(order);
					break;
				case FILLED:
					logOrderStatus("Completed order. ", order);
					orderFinalized(null, order);
					break;
				case CANCELLED:
					logOrderStatus("Could not create order. ", order);
					orderFinalized(null, order);
					break;
			}
		}
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	private OrderRequest prepareOrder(TradingManager tradingManager, Order.Side side, Trade.Side tradeSide, double quantity, Order resubmissionFrom, Trade trade) {
		SymbolPriceDetails priceDetails = tradingManager.getPriceDetails();
		long time = tradingManager.getLatestCandle().closeTime;
		OrderRequest orderPreparation = new OrderRequest(tradingManager.getAssetSymbol(), tradingManager.getFundSymbol(), side, tradeSide, time, resubmissionFrom);
		orderPreparation.setPrice(tradingManager.getLatestPrice());

		if (tradeSide == LONG) {
			if (orderPreparation.isSell()) {
				double availableAssets = getAmount(orderPreparation.getAssetsSymbol());
				if (availableAssets < quantity) {
					quantity = availableAssets;
				}
			}
		}

		orderPreparation.setQuantity(quantity);

		OrderBook book = account.getOrderBook(tradingManager.getSymbol(), 0);


		OrderManager orderCreator = configuration.orderManager(tradingManager.getSymbol());
		if (orderCreator != null) {
			orderCreator.prepareOrder(priceDetails, book, orderPreparation, tradingManager.trader, trade);
		}

		if (!orderPreparation.isCancelled() && orderPreparation.getTotalOrderAmount() > (priceDetails.getMinimumOrderAmount(orderPreparation.getPrice()))) {
			orderPreparation.setPrice(orderPreparation.getPrice());
			orderPreparation.setQuantity(orderPreparation.getQuantity());

			boolean closingShort = tradeSide == SHORT && side == BUY;
			double minimumInvestment = configuration.minimumInvestmentAmountPerTrade(orderPreparation.getAssetsSymbol());
			double orderAmount = orderPreparation.getTotalOrderAmount() * 1.01 + getTradingFees().feesOnOrder(orderPreparation);

			if ((orderAmount >= minimumInvestment || closingShort) && orderAmount > priceDetails.getMinimumOrderAmount(orderPreparation.getPrice())) {
				return orderPreparation;
			}
		}

		return null;
	}

	TradingManager getTradingManagerOf(String symbol) {
		return allTradingManagers.get(symbol);
	}

	public Trader getTraderOf(String symbol) {
		return traders.computeIfAbsent(symbol, (s) -> {
			TradingManager tradingManager = getTradingManagerOf(symbol);
			if (tradingManager == null) {
				return null;
			}
			return tradingManager.trader;
		});
	}

	void register(TradingManager tradingManager) {
		this.allTradingManagers.put(tradingManager.getSymbol(), tradingManager);
	}

	private Trader getTraderOfSymbol(String symbol) {
		TradingManager a = allTradingManagers.get(symbol);
		if (a != null) {
			return a.trader;
		}
		return null;
	}

	public TradingManager[] getAllTradingManagers() {
		if (tradingManagers == null) {
			if (allTradingManagers.isEmpty()) {
				throw new IllegalStateException("Can't calculate total funds in account '" + configuration.id() + "'. Available symbols are: " + configuration.symbols());
			}
			this.tradingManagers = allTradingManagers.values().toArray(new TradingManager[0]);
		}
		return tradingManagers;
	}

	public Collection<String[]> getTradedPairs() {
		return configuration.tradedWithPairs();
	}

	public Collection<String> getTradedAssetSymbols() {
		return configuration.tradedWithPairs().stream().map(p -> p[0]).collect(Collectors.toList());
	}

	public Collection<String> getTradedSymbols() {
		return configuration.tradedWithPairs().stream().map(p -> p[0] + p[1]).collect(Collectors.toList());
	}

	public Collection<String> getTradedFundSymbols() {
		return configuration.tradedWithPairs().stream().map(p -> p[1]).collect(Collectors.toList());
	}

	public TradingFees getTradingFees() {
		return ClientAccount.super.getTradingFees();
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return account.updateOrderStatus(order);
	}

	@Override
	public void cancel(Order order) {
		account.cancel(order);
	}

	private void logOrderStatus(String msg, Order order) {
		if (log.isTraceEnabled()) {
			Long tradeId = order.getTrade() != null ? order.getTrade().id() : null;


			//e.g. PARTIALLY_FILLED LIMIT BUY of 1 BTC @ 9000 USDT each after 10 seconds.
			log.trace("Trade[{}] {}{} {} {} of {}/{} {} @ {} {} each after {}. Order id: {}, order quantity: {}, amount: ${} of expected ${} {}",
					tradeId,
					msg,
					order.getStatus(),
					order.getType(),
					order.getSide(),
					BigDecimal.valueOf(order.getExecutedQuantity()).setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getQuantity(),
					order.getAssetsSymbol(),
					BigDecimal.valueOf(order.getAveragePrice()).setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getFundsSymbol(),
					TimeInterval.getFormattedDuration(System.currentTimeMillis() - order.getTime()),
					order.getOrderId(),
					BigDecimal.valueOf(order.getQuantity()).setScale(8, RoundingMode.FLOOR).toPlainString(),
					BigDecimal.valueOf(order.getTotalTraded()).setScale(8, RoundingMode.FLOOR).toPlainString(),
					BigDecimal.valueOf(order.getTotalOrderAmount()).setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getFundsSymbol());
		}
	}

	@Override
	public final boolean isSimulated() {
		return account.isSimulated();
	}

	public void removePendingOrder(Order order) {
		pendingOrders.remove(order);
		synchronized (orderUpdates) {
			orderUpdates.remove(order);
		}
	}

	public void waitForFill(Order order) {
		pendingOrders.addOrReplace(order);
		if (isSimulated()) {
			return;
		}
		new Thread(() -> {
			Thread.currentThread().setName("Order " + order.getOrderId() + " monitor: " + order.getSide() + " " + order.getSymbol());
			OrderManager orderManager = configuration.orderManager(order.getSymbol());
			Order o = order;
			while (true) {
				try {
					try {
						Thread.sleep(orderManager.getOrderUpdateFrequency().ms);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					if (!o.isFinalized()) {
						o = account.updateOrderStatus(o);
					} else {
						o = null;
					}
					if (o != null) {
						if (order.getTrade() == null) {
							throw new IllegalStateException("Trade associated with order " + order + " can't be null");
						}
						synchronized (orderUpdates) {
							orderUpdates.addOrReplace(o);
						}
					}
					if (o == null || o.isFinalized()) {
						return;
					}
				} catch (Exception e) {
					log.error("Error tracking state of order " + o, e);
					return;
				}
			}
		}).start();
	}

	protected void updateOpenOrders(String symbol, Candle candle) {
		updateOpenOrders(symbol);
	}

	public void updateOpenOrders(String symbol) {
		synchronized (orderUpdates) {
			for (int i = orderUpdates.i - 1; i >= 0; i--) {
				Order order = orderUpdates.elements[i];
				if (symbol.equals(order.getSymbol())) {
					updateOrder(order);
				}
			}
		}
	}

	public Order updateOrder(Order order) {
		OrderManager orderManager = configuration.orderManager(order.getSymbol());

		if (order.getTrade() != null) {
			if (!order.getTrade().orderUpdated(order)) {
				removePendingOrder(order);
				return order;
			}
		}

		Order update = order;
		if (!isSimulated()) {
			if (pendingOrders.contains(order)) {
				order = pendingOrders.get(order);
			}
			synchronized (orderUpdates) {
				update = orderUpdates.get(order);
			}
			if (update == null) {
				log.warn("Lost track of order {}. Trying to cancel it.", order);
				update = cancelOrder(orderManager, order);
			}
		}

		if (update.isFinalized()) {
			logOrderStatus("Order finalized. ", update);
			removePendingOrder(update);
			orderFinalized(orderManager, update);
			return update;
		} else { // update order status
			pendingOrders.addOrReplace(update);
		}

		if (update.getExecutedQuantity() != order.getExecutedQuantity() || (isSimulated() && update instanceof DefaultOrder && ((DefaultOrder) update).hasPartialFillDetails())) {
			logOrderStatus("Order updated. ", update);
			executeUpdateBalances();
			orderManager.updated(update, traderOf(update), this::resubmit);
		} else {
			logOrderStatus("Unchanged ", update);
			orderManager.unchanged(update, traderOf(update), this::resubmit);
		}

		//order manager could have cancelled the order
		if (update.getStatus() == CANCELLED && pendingOrders.contains(update)) {
			cancelOrder(orderManager, update);
		}
		return update;
	}

	private void orderFinalized(OrderManager orderManager, Order order) {
		orderManager = orderManager == null ? configuration.orderManager(order.getSymbol()) : orderManager;
		try {
			executeUpdateBalances();
		} finally {
			Trader trader;
			if (order.getTrade() != null) {
				trader = order.getTrade().trader();
			} else {
				trader = traderOf(order);
			}
			notifyFinalized(orderManager, order, trader);
			List<Order> attachments;
			if (order.getParent() != null) {
				attachments = order.getParent().getAttachments();
			} else {
				attachments = order.getAttachments();
			}
			if (attachments != null && order.isCancelled() && order.getExecutedQuantity() == 0.0) {
				for (Order attached : attachments) {
					if (attached != order) {
						attached.cancel();
						notifyFinalized(orderManager, attached, trader);
					}
				}
			}
		}
	}

	private void notifyFinalized(OrderManager orderManager, Order order, Trader trader) {
		try {
			orderManager.finalized(order, trader);
		} finally {
			getTradingManagerOf(order.getSymbol()).notifyOrderFinalized(order);
		}
	}

	private void resubmit(Order order) {
		if (order == null) {
			throw new IllegalArgumentException("Order for resubmission cannot be null");
		}

		if (order.getFillPct() > 98.0) {
			//ignore orders 98% filled.
			return;
		}


		OrderManager orderManager = configuration.orderManager(order.getSymbol());
		cancelOrder(orderManager, order);

		TradingManager tradingManager = getTradingManagerOf(order.getSymbol());
		Trade trade = order.getTrade();

		tradingManager.updateOpenOrders(order.getSymbol(), tradingManager.trader.latestCandle());

		OrderRequest request = prepareOrder(tradingManager, order.getSide(), order.getTradeSide(), order.getRemainingQuantity(), order, trade);
		order = executeOrder(request);

		Strategy strategy = tradingManager.getTrader().strategyOf(order);

		tradingManager.trader.processOrder(trade, order, strategy, trade == null ? "Order resubmission" : "Order resubmission: " + trade.exitReason());
	}

	public Order submitOrder(Trader trader, double quantity, Order.Side side, Trade.Side tradeSide, Order.Type type, Trade trade) {
		OrderRequest request = prepareOrder(trader.tradingManager, side, tradeSide, quantity, null, trade);
		Order order = executeOrder(request);
		trader.processOrder(null, order, null, null);
		return order;
	}

	private Trader traderOf(Order order) {
		return getTraderOfSymbol(order.getSymbol());
	}

	private Order cancelOrder(OrderManager orderManager, Order order) {
		try {
			order.cancel();
			account.cancel(order);
		} catch (Exception e) {
			log.error("Failed to execute cancellation of order '" + order + "' on exchange", e);
		} finally {
			removePendingOrder(order);
			order = account.updateOrderStatus(order);
			orderFinalized(orderManager, order);
			logOrderStatus("Cancellation via order manager: ", order);
		}
		return order;
	}

	public Order cancelOrder(Order order) {
		return cancelOrder(configuration.orderManager(order.getSymbol()), order);
	}

	public void cancelStaleOrdersFor(Trader trader) {
		if (pendingOrders.isEmpty()) {
			return;
		}
		for (int i = pendingOrders.i - 1; i >= 0; i--) {
			Order order = pendingOrders.elements[i];
			if (order.isFinalized()) {
				continue;
			}
			OrderManager orderManager = configuration.orderManager(order.getSymbol());
			Trader traderOfOrder = traderOf(order);
			if (traderOfOrder == null) {
				log.warn("Cancelling order {} as no trader found for this order", order);
				order.cancel();
			} else if (traderOfOrder.latestCandle() == null) {
				log.warn("Cancelling order {} as latest candle information is available for this symbol", order);
				order.cancel();
			}

			if (order.isCancelled() || traderOfOrder != null && orderManager.cancelToReleaseFundsFor(order, traderOfOrder, trader)) {
				if (order.getStatus() == CANCELLED) {
					cancelOrder(orderManager, order);
					return;
				}
			}
		}
	}

	public AccountConfiguration<?> configuration() {
		return configuration;
	}

	public boolean canShortSell() {
		return configuration().shortingEnabled();
	}

	public final double marginReserveFactorPct() {
		return marginReserveFactorPct;
	}

	public AtomicLong getTradeIdGenerator() {
		return tradeIdGenerator;
	}

	public Map<String, Balance> getBalanceSnapshot() {
		balanceLock.lock();
		try {
			return balances.entrySet()
					.stream()
					.collect(Collectors
							.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
		} finally {
			balanceLock.unlock();
		}
	}
}
