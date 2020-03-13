package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

public class AccountManager implements ClientAccount, SimulatedAccountConfiguration {
	private static final Logger log = LoggerFactory.getLogger(AccountManager.class);

	private final Map<Integer, long[]> fundAllocationCache = new ConcurrentHashMap<>();
	private static final AtomicLong NULL = new AtomicLong(0);

	private final Map<String, Trader> traders = new ConcurrentHashMap<>();
	private final AccountConfiguration<?> configuration;

	private final OrderSet pendingOrders = new OrderSet();

	private final Set<String> lockedPairs = ConcurrentHashMap.newKeySet();

	private static final long BALANCE_EXPIRATION_TIME = minutes(10).ms;
	private static final long FREQUENT_BALANCE_UPDATE_INTERVAL = seconds(15).ms;

	private long lastBalanceSync = 0L;
	private final Map<String, Balance> balances = new ConcurrentHashMap<>();
	private Balance[] balancesArray;

	private final ExchangeClient client;
	private final ClientAccount account;
	private final Map<String, TradingManager> allTradingManagers = new ConcurrentHashMap<>();
	private TradingManager[] tradingManagers;
	private final Simulation simulation;
	private final double marginReserveFactor;
	private final double marginReserveFactorPct;
	private final int accountHash;

	public AccountManager(ClientAccount account, AccountConfiguration<?> configuration, Simulation simulation) {
		if (StringUtils.isBlank(configuration.referenceCurrency())) {
			throw new IllegalConfigurationException("Please configure the reference currency symbol");
		}
		if (configuration.symbolPairs().isEmpty()) {
			throw new IllegalConfigurationException("Please configure traded symbol pairs");
		}
		this.accountHash = configuration.id().hashCode();
		this.simulation = simulation;
		this.account = account;
		this.configuration = configuration;
		this.marginReserveFactor = account.marginReservePercentage() / 100.0;
		this.marginReserveFactorPct = marginReserveFactor;
		this.client = new ExchangeClient(this);

		if (account.marginReservePercentage() < 100) {
			throw new IllegalStateException("Margin reserve percentage must be at least 100%");
		}
	}

	public ExchangeClient getClient() {
		return client;
	}

	public Map<String, Balance> getBalances() {
		long now = System.currentTimeMillis();
		if ((now - lastBalanceSync) > BALANCE_EXPIRATION_TIME) {
			lastBalanceSync = now;
			updateBalances();
		}
		return balances;
	}

	public double applyMarginReserve(double amount) {
		return amount * marginReserveFactor;
	}

	/**
	 * Returns the amount held in the account for the given symbol.
	 *
	 * @param symbol the symbol whose amount will be returned
	 *
	 * @return the amount held for the given symbol.
	 */
	public double getAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getFree();
	}

	public double getShortedAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getShorted();
	}

	public Balance getBalance(String symbol) {
		return balances.computeIfAbsent(symbol.trim(), (s) -> {
			synchronized (balances) {
				balancesArray = null;
				return new Balance(symbol);
			}
		});
	}

	public void subtractFromFreeBalance(String symbol, final double amount) {
		Balance balance = getBalance(symbol);
		balance.setFree(balance.getFree() - amount);
	}

	public void subtractFromLockedBalance(String symbol, final double amount) {
		Balance balance = getBalance(symbol);
		balance.setLocked(balance.getLocked() - amount);
	}

	public void releaseFromLockedBalance(String symbol, final double amount) {
		Balance balance = getBalance(symbol);
		double locked = balance.getLocked();
		if (locked < amount) {
			double toTakeFromFreeBalance = amount - locked;
			locked -= toTakeFromFreeBalance;

			balance.setLocked(0.0);
			balance.setFree(balance.getFree() + locked);
		} else {
			balance.setLocked(locked - amount);
			balance.setFree(balance.getFree() + amount);
		}
	}

	public void subtractFromShortedBalance(String symbol, final double amount) {
		Balance balance = getBalance(symbol);
		balance.setShorted(balance.getShorted() - amount);
	}

	public double getMarginReserve(String fundSymbol, String assetSymbol) {
		return getBalance(fundSymbol).getMarginReserve(assetSymbol);

	}

	public void subtractFromMarginReserveBalance(String fundSymbol, String assetSymbol, final double amount) {
		Balance balance = getBalance(fundSymbol);
		balance.setMarginReserve(assetSymbol, balance.getMarginReserve(assetSymbol) - amount);
	}

	public void subtractFromLockedOrFreeBalance(String funds, double amount) {
		subtractFromLockedOrFreeBalance(funds, null, amount);
	}

	public void subtractFromLockedOrFreeBalance(String funds, String asset, double amount) {
		Balance balance = getBalance(funds);
		double locked = balance.getLocked();

		if (amount < locked) { //got more locked funds
			subtractFromLockedBalance(funds, amount);
		} else { //clear locked funds. Account reserve is greater than locked amount when price jumps a bit
			subtractFromLockedBalance(funds, locked);
			double remainder = amount - locked;
			if (balance.getFree() >= remainder) {
				subtractFromFreeBalance(funds, remainder);
			} else if (asset != null) { //use margin
				subtractFromMarginReserveBalance(funds, asset, remainder);
			} else {
				//will throw exception.
				subtractFromLockedBalance(funds, amount);
			}
		}
	}

	public synchronized void addToLockedBalance(String symbol, double amount) {
		Balance balance = balances.get(symbol);
		if (balance == null) {
			balance = new Balance(symbol);
			balances.put(symbol, balance);
		}
		amount = balance.getLocked() + amount;
		balance.setLocked(amount);
	}

	//TODO: need to implement margin release/call according to price movement.
	public void addToMarginReserveBalance(String fundSymbol, String assetSymbol, double amount) {
		Balance balance = balances.get(fundSymbol);
		amount = balance.getMarginReserve(assetSymbol) + amount;
		balance.setMarginReserve(assetSymbol, amount);
	}


	public void addToFreeBalance(String symbol, double amount) {
		Balance balance = getBalance(symbol);
		amount = balance.getFree() + amount;
		balance.setFree(amount);
	}

	public void addToShortedBalance(String symbol, double amount) {
		Balance balance = getBalance(symbol);
		amount = balance.getShorted() + amount;
		balance.setShorted(amount);
	}

	@Override
	public synchronized AccountManager setAmount(String symbol, double amount) {
		if (configuration.isSymbolSupported(symbol)) {
			synchronized (balances) {
				balances.put(symbol, new Balance(symbol, amount));
				this.balancesArray = null;
			}
			return this;
		}
		throw configuration.reportUnknownSymbol("Can't set funds", symbol);
	}

	public synchronized AccountManager lockAmount(String symbol, double amount) {
		if (configuration.isSymbolSupported(symbol)) {
			subtractFromFreeBalance(symbol, amount);
			addToLockedBalance(symbol, amount);
			return this;
		}
		throw configuration.reportUnknownSymbol("Can't set funds", symbol);
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
		int hash = hash(assetSymbol, fundSymbol, tradeSide);
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

	public synchronized double getTotalFundsInReferenceCurrency() {
		return getTotalFundsIn(configuration.referenceCurrency());
	}

	public synchronized double getTotalFundsIn(String currency) {
		final Balance[] tmp;
		synchronized (balances) {
			if (balancesArray == null) {
				tmp = balancesArray = balances.values().toArray(new Balance[0]);
			} else {
				tmp = balancesArray;
			}
		}

		double total = 0.0;
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
				double shortedQuantity = balances.get(shorted).getShorted();
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

	public boolean waitingForFill(String assetSymbol, Order.Side side) {
		for (int i = 0; i < pendingOrders.i; i++) {
			Order order = pendingOrders.elements[i];
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

	public boolean isBuyLocked(String assetSymbol) {
		synchronized (lockedPairs) {
			if (lockedPairs.contains(assetSymbol)) {
				return true;
			}
			if (waitingForFill(assetSymbol, BUY)) {
				return true;
			}
			return false;
		}
	}

	public boolean isShortSellLocked(String assetSymbol) {
		synchronized (lockedPairs) {
			if (lockedPairs.contains(assetSymbol)) {
				return true;
			}
			if (waitingForFill(assetSymbol, SELL)) {
				return true;
			}
			return false;
		}
	}

	private void lockTrading(String assetSymbol) {
		synchronized (lockedPairs) {
			if (log.isTraceEnabled()) {
				log.trace("Locking trading on {}", assetSymbol);
			}
			lockedPairs.add(assetSymbol);
		}
	}

	private void unlockTrading(String assetSymbol) {
		synchronized (lockedPairs) {
			if (lockedPairs.contains(assetSymbol)) {
				if (log.isTraceEnabled()) {
					log.trace("Unlocking trading on {}", assetSymbol);
				}
				lockedPairs.remove(assetSymbol);
			}
		}
	}


	@Override
	public synchronized String toString() {
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
		Map<String, Balance> updatedBalances = account.updateBalances();
		if (updatedBalances != null && updatedBalances != balances) {
			updatedBalances.keySet().retainAll(configuration.symbols());
			synchronized (balances) {
				this.balances.clear();
				this.balances.putAll(updatedBalances);
				this.balancesArray = null;
			}

			updatedBalances.values().removeIf(b -> b.getTotal() == 0);
			log.debug("Balances updated: " + updatedBalances);

			lastBalanceSync = System.currentTimeMillis();
		}
	}

	@Override
	public synchronized Map<String, Balance> updateBalances() {
		long now = System.currentTimeMillis();
		if (now - lastBalanceSync < FREQUENT_BALANCE_UPDATE_INTERVAL) {
			return balances;
		}
		executeUpdateBalances();

		return balances;
	}

	public String getReferenceCurrencySymbol() {
		return configuration.referenceCurrency();
	}

	public Order buy(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity) {
		synchronized (lockedPairs) {
			if (!isBuyLocked(assetSymbol)) {
				try {
					lockTrading(assetSymbol);
					String symbol = assetSymbol + fundSymbol;
					TradingManager tradingManager = getTradingManagerOf(symbol);
					if (tradingManager == null) {
						throw new IllegalStateException("Unable to buy " + quantity + " units of unknown symbol: " + symbol);
					}
					if (tradeSide == SHORT) {
						OrderRequest orderPreparation = prepareOrder(tradingManager, BUY, SHORT, quantity, null);
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
						OrderRequest orderPreparation = prepareOrder(tradingManager, BUY, tradeSide, quantity, null);
						return executeOrder(orderPreparation);
					}
				} finally {
					unlockTrading(assetSymbol);
				}
			}
			return null;
		}
	}

	public Order sell(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity) {
		String symbol = assetSymbol + fundSymbol;
		TradingManager tradingManager = getTradingManagerOf(symbol);
		if (tradingManager == null) {
			throw new IllegalStateException("Unable to sell " + quantity + " units of unknown symbol: " + symbol);
		}
		OrderRequest orderPreparation = prepareOrder(tradingManager, SELL, tradeSide, quantity, null);
		return executeOrder(orderPreparation);
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		if (orderDetails != null) {
			if (orderDetails.isCancelled()) {
				return null;
			}
			if (orderDetails.getQuantity() == 0) {
				throw new IllegalArgumentException("Not quantity specified for order " + orderDetails);
			}
			if (orderDetails.getPrice() == 0 && orderDetails.getType() == Order.Type.LIMIT) {
				throw new IllegalArgumentException("Not price specified for LIMIT order " + orderDetails);
			}

			Order order = account.executeOrder(orderDetails);
			if (order != null) {
				switch (order.getStatus()) {
					case NEW:
					case PARTIALLY_FILLED:
						logOrderStatus("Tracking pending order. ", order);
						waitForFill(order);
						return order;
					case FILLED:
						logOrderStatus("Completed order. ", order);
						orderFinalized(null, order, null);
						return order;
					case CANCELLED:
						logOrderStatus("Could not create order. ", order);
						orderFinalized(null, order, null);
						return null;
				}
			}
		}
		return null;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	private OrderRequest prepareOrder(TradingManager tradingManager, Order.Side side, Trade.Side tradeSide, double quantity, Order resubmissionFrom) {
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
			orderCreator.prepareOrder(priceDetails, book, orderPreparation, tradingManager.getLatestCandle());
		}

		if (!orderPreparation.isCancelled() && orderPreparation.getTotalOrderAmount() > (priceDetails.getMinimumOrderAmount(orderPreparation.getPrice()))) {
			orderPreparation.setPrice(orderPreparation.getPrice());
			orderPreparation.setQuantity(orderPreparation.getQuantity());

			if (orderPreparation.getTotalOrderAmount() > priceDetails.getMinimumOrderAmount(orderPreparation.getPrice())) {
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
		if (simulation != null) {
			if (simulation.tradingFees() == null) {
				throw new IllegalConfigurationException("Please configure trading fess");
			}
			return simulation.tradingFees();
		}
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

	private static void logOrderStatus(String msg, Order order) {
		if (log.isTraceEnabled()) {
			//e.g. PARTIALLY_FILLED LIMIT BUY of 1 BTC @ 9000 USDT each after 10 seconds.
			log.trace("{}{} {} {} of {}/{} {} @ {} {} each after {}. Order id: {}, order quantity: {}, amount: ${} of expected ${} {}",
					msg,
					order.getStatus(),
					order.getType(),
					order.getSide(),
					order.getQuantity(),
					BigDecimal.valueOf(order.getExecutedQuantity()).setScale(8, RoundingMode.FLOOR).toPlainString(),
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

	public void waitForFill(Order order) {
		pendingOrders.add(order);
		if (isSimulated()) {
			return;
		}
		new Thread(() -> {
			Thread.currentThread().setName("Order " + order.getOrderId() + " monitor:" + order.getSide() + " " + order.getSymbol());
			OrderManager orderManager = configuration.orderManager(order.getSymbol());
			while (true) {
				try {
					try {
						Thread.sleep(orderManager.getOrderUpdateFrequency().ms);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					Order updated = updateOrder(order, null);
					if (updated.isFinalized()) {
						return;
					}
				} catch (Exception e) {
					log.error("Error tracking state of order " + order, e);
					return;
				}
			}
		}).start();
	}

	public Order updateOrder(Order order, Trade trade) {
		OrderManager orderManager = configuration.orderManager(order.getSymbol());
		Order old = order;
		order = account.updateOrderStatus(order);

		if (order.isFinalized()) {
			logOrderStatus("", order);
			pendingOrders.remove(order);
			orderFinalized(orderManager, order, trade);
			return order;
		} else { // update order status
			pendingOrders.addOrReplace(order);
		}

		if (old.getExecutedQuantity() != order.getExecutedQuantity() || (isSimulated() && order instanceof DefaultOrder && ((DefaultOrder) order).hasPartialFillDetails())) {
			logOrderStatus("", order);
			executeUpdateBalances();
			orderManager.updated(order, traderOf(order), this::resubmit);
		} else {
			logOrderStatus("Unchanged ", order);
			orderManager.unchanged(order, traderOf(order), this::resubmit);
		}

		//order manager could have cancelled the order
		if (order.getStatus() == CANCELLED && pendingOrders.contains(order)) {
			cancelOrder(orderManager, order);
		}
		return order;
	}

	private void orderFinalized(OrderManager orderManager, Order order, Trade trade) {
		orderManager = orderManager == null ? configuration.orderManager(order.getSymbol()) : orderManager;
		try {
			executeUpdateBalances();
		} finally {
			Trader trader;
			if (trade != null) {
				trader = trade.trader();
			} else {
				trader = traderOf(order);
				trade = trader.tradeOf(order);
			}
			notifyFinalized(orderManager, order, trade, trader);
			if (order.getAttachments() != null && order.isCancelled() && order.getExecutedQuantity() == 0.0) {
				for (Order attached : order.getAttachments()) {
					attached.cancel();
					notifyFinalized(orderManager, attached, trade, trader);
				}
			}
		}
	}

	private void notifyFinalized(OrderManager orderManager, Order order, Trade trade, Trader trader) {
		try {
			orderManager.finalized(order, trader);
		} finally {
			getTradingManagerOf(order.getSymbol()).notifyOrderFinalized(order, trade);
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
		Trade trade = tradingManager.getTrader().tradeOf(order);

		tradingManager.updateOpenOrders(order.getSymbol(), tradingManager.trader.latestCandle());

		OrderRequest request = prepareOrder(tradingManager, order.getSide(), order.getTradeSide(), order.getRemainingQuantity(), order);
		order = executeOrder(request);

		Strategy strategy = tradingManager.getTrader().strategyOf(order);

		tradingManager.trader.processOrder(trade, order, strategy, trade == null ? "Order resubmission" : "Order resubmission: " + trade.exitReason());
	}

	public Order submitOrder(Trader trader, double quantity, Order.Side side, Trade.Side tradeSide, Order.Type type) {
		OrderRequest request = prepareOrder(trader.tradingManager, side, tradeSide, quantity, null);
		Order order = executeOrder(request);
		trader.processOrder(null, order, null, null);
		return order;
	}

	private Trader traderOf(Order order) {
		return getTraderOfSymbol(order.getSymbol());
	}

	private void cancelOrder(OrderManager orderManager, Order order) {
		try {
			order.cancel();
			account.cancel(order);
		} catch (Exception e) {
			log.error("Failed to execute cancellation of order '" + order + "' on exchange", e);
		} finally {
			order = account.updateOrderStatus(order);
			pendingOrders.remove(order);
			orderFinalized(orderManager, order, null);
			logOrderStatus("Cancellation via order manager: ", order);
		}
	}

	public synchronized void cancelOrder(Order order) {
		OrderManager orderManager = configuration.orderManager(order.getSymbol());
		if (!order.isFinalized()) {
			Order latestUpdate = pendingOrders.get(order);
			if (latestUpdate != null) {
				order = latestUpdate;
			}
			if (!order.isFinalized()) {
				cancelOrder(orderManager, order);
			}
		}
	}

	public synchronized void cancelStaleOrdersFor(Trader trader) {
		if (pendingOrders.isEmpty()) {
			return;
		}
		for (int i = 0; i < pendingOrders.i; i++) {
			Order order = pendingOrders.elements[i];
			OrderManager orderManager = configuration.orderManager(order.getSymbol());
			if (orderManager.cancelToReleaseFundsFor(order, traderOf(order), trader)) {
				if (order.getStatus() == CANCELLED) {
					cancelOrder(orderManager, order);
					return;
				}
			}
		}
	}

	public SimulatedAccountConfiguration resetBalances() {
		synchronized (balances) {
			this.balances.clear();
			this.balancesArray = null;
		}
		executeUpdateBalances();
		return this;
	}

	public boolean updateOpenOrders(String symbol, Candle candle) {
		if (this.account.updateOpenOrders(symbol, candle)) {
			for (int i = 0; i < pendingOrders.i; i++) {
				Order order = pendingOrders.elements[i];
				if (symbol.equals(order.getSymbol())) {
					updateOrder(order, null);
				}
			}
			return true;
		}
		return false;
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
}
