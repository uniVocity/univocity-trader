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
import java.util.stream.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

public class AccountManager implements ClientAccount, SimulatedAccountConfiguration {
	private static final Logger log = LoggerFactory.getLogger(AccountManager.class);

	private final AccountConfiguration<?> configuration;
	private final Map<String, Order> pendingOrders = new ConcurrentHashMap<>();
	private final Set<String> lockedPairs = ConcurrentHashMap.newKeySet();

	private static final long BALANCE_EXPIRATION_TIME = minutes(10).ms;
	private static final long FREQUENT_BALANCE_UPDATE_INTERVAL = seconds(15).ms;

	private long lastBalanceSync = 0L;
	private final Map<String, Balance> balances = new ConcurrentHashMap<>();

	private final ExchangeClient client;
	private final ClientAccount account;
	private final Map<String, TradingManager> allTradingManagers = new ConcurrentHashMap<>();
	private final Simulation simulation;
	private final BigDecimal marginReserveFactor;
	private final double marginReserveFactorPct;

	public AccountManager(ClientAccount account, AccountConfiguration<?> configuration, Simulation simulation) {
		if (StringUtils.isBlank(configuration.referenceCurrency())) {
			throw new IllegalConfigurationException("Please configure the reference currency symbol");
		}
		if (configuration.symbolPairs().isEmpty()) {
			throw new IllegalConfigurationException("Please configure traded symbol pairs");
		}
		this.simulation = simulation;
		this.account = account;
		this.configuration = configuration;
		this.marginReserveFactor = round(BigDecimal.valueOf(account.marginReservePercentage()).divide(BigDecimal.valueOf(100), ROUND_MC));
		this.marginReserveFactorPct = marginReserveFactor.doubleValue();
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

	public BigDecimal applyMarginReserve(BigDecimal amount) {
		return round(amount.multiply(marginReserveFactor));
	}

	/**
	 * Returns the amount held in the account for the given symbol.
	 *
	 * @param symbol the symbol whose amount will be returned
	 *
	 * @return the amount held for the given symbol.
	 */
	public double getAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getFreeAmount();
	}

	/**
	 * Returns the amount held in the account for the given symbol.
	 *
	 * @param symbol the symbol whose amount will be returned
	 *
	 * @return the amount held for the given symbol.
	 */
	public BigDecimal getPreciseAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getFree();
	}

	public double getShortedAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getShortedAmount();
	}

	public BigDecimal getPreciseShortedAmount(String symbol) {
		return balances.getOrDefault(symbol, Balance.ZERO).getShorted();
	}

	public Balance getBalance(String symbol) {
		return balances.computeIfAbsent(symbol.trim(), Balance::new);
	}

	public void subtractFromFreeBalance(String symbol, final BigDecimal amount) {
		Balance balance = getBalance(symbol);
		BigDecimal result = round(balance.getFree().subtract(amount));
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			if (result.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Can't subtract " + amount + " from " + symbol + "'s current free balance of: " + balance.getFree() + ". Insufficient funds.");
			} else {
				result = BigDecimal.ZERO;
			}
		}
		balance.setFree(result);
	}

	public void subtractFromLockedBalance(String symbol, final BigDecimal amount) {
		Balance balance = getBalance(symbol);
		BigDecimal result = round(balance.getLocked().subtract(amount));
		if (result.compareTo(BigDecimal.ZERO) < 0) {
			if (result.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Can't subtract " + amount + " from " + symbol + "'s current locked balance of: " + balance.getLocked() + ". Insufficient funds.");
			} else {
				result = BigDecimal.ZERO;
			}
		}
		balance.setLocked(result);
	}

	public void subtractFromShortedBalance(String symbol, final BigDecimal amount) {
		Balance balance = getBalance(symbol);
		BigDecimal result = round(balance.getShorted().subtract(amount));
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			if (result.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Can't subtract " + amount + " from " + symbol + "'s current short balance of: " + balance.getShorted() + ". Insufficient funds.");
			} else {
				result = BigDecimal.ZERO;
			}
		}
		balance.setShorted(result);
	}

	public BigDecimal getMarginReserve(String fundSymbol, String assetSymbol) {
		return getBalance(fundSymbol).getMarginReserve(assetSymbol);

	}

	public void subtractFromMarginReserveBalance(String fundSymbol, String assetSymbol, final BigDecimal amount) {
		Balance balance = getBalance(fundSymbol);
		BigDecimal result = round(balance.getMarginReserve(assetSymbol).subtract(amount));
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			if (result.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Can't subtract " + amount + " from " + fundSymbol + "'s margin reserve: " + balance.getMarginReserve(assetSymbol) + ". Insufficient funds.");
			} else {
				result = BigDecimal.ZERO;
			}
		}
		balance.setMarginReserve(assetSymbol, result);
	}

	private void addToLockedBalance(String symbol, BigDecimal amount) {
		Balance balance = balances.get(symbol);
		if (balance == null) {
			throw new IllegalStateException("Can't lock " + amount + " " + symbol + ". No balance available.");
		}
		amount = balance.getLocked().add(amount);
		balance.setLocked(amount);
	}

	//TODO: need to implement margin release/call according to price movement.
	public void addToMarginReserveBalance(String fundSymbol, String assetSymbol, BigDecimal amount) {
		Balance balance = balances.get(fundSymbol);
		amount = balance.getMarginReserve(assetSymbol).add(amount);
		balance.setMarginReserve(assetSymbol, amount);
	}


	public void addToFreeBalance(String symbol, BigDecimal amount) {
		Balance balance = getBalance(symbol);
		amount = balance.getFree().add(amount);
		balance.setFree(amount);
	}

	public void addToShortedBalance(String symbol, BigDecimal amount) {
		Balance balance = getBalance(symbol);
		amount = balance.getShorted().add(amount);
		balance.setShorted(amount);
	}

	@Override
	public synchronized AccountManager setAmount(String symbol, double amount) {
		if (configuration.isSymbolSupported(symbol)) {
			balances.put(symbol, new Balance(symbol, amount));
			return this;
		}
		throw configuration.reportUnknownSymbol("Can't set funds", symbol);
	}

	public synchronized AccountManager lockAmount(String symbol, BigDecimal amount) {
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


	public double allocateFunds(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
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
			out = 0.0;
		}

		return out > 0.0 ? getTradingFees().takeFee(out, Order.Type.LIMIT, SELL) : 0.0;
	}

	public double allocateFunds(String assetSymbol, Trade.Side tradeSide) {
		return allocateFunds(assetSymbol, getReferenceCurrencySymbol(), tradeSide);
	}

	public synchronized double getTotalFundsInReferenceCurrency() {
		return getTotalFundsIn(configuration.referenceCurrency());
	}

	public synchronized double getTotalFundsIn(String currency) {
		if (allTradingManagers.isEmpty()) {
			throw new IllegalStateException("Can't calculate total funds in " + currency + " as account '" + configuration.id() + "' doesn't handle this symbol. Available symbols are: " + configuration.symbols());
		}

		double total = 0.0;
		Map<String, Double> allPrices = allTradingManagers.values().iterator().next().getAllPrices();
		Map<String, Balance> positions = balances;
		for (var e : positions.entrySet()) {
			String symbol = e.getKey();
			double quantity = e.getValue().getTotal().doubleValue();


			for (String shorted : e.getValue().getShortedAssetSymbols()) {
				double reserve = e.getValue().getMarginReserve(shorted).doubleValue();
				double marginWithoutReserve = e.getValue().getMarginReserve(shorted).doubleValue() / marginReserveFactorPct;
				double shortedQuantity = balances.get(shorted).getShortedAmount();
				double originalShortedPrice = marginWithoutReserve / shortedQuantity;
				double totalInvestmentOnShort = shortedQuantity * originalShortedPrice;
				double totalAtCurrentPrice = multiplyWithLatestPrice(shortedQuantity, shorted, symbol, allPrices);
				double shortProfitLoss = totalInvestmentOnShort - totalAtCurrentPrice;

				total += shortProfitLoss;
				total += (reserve - marginWithoutReserve);
			}

			if (currency.equals(symbol)) {
				total += quantity;
			} else {
				total += multiplyWithLatestPrice(quantity, symbol, currency, allPrices);
			}
		}
		return total;
	}

	private double multiplyWithLatestPrice(double quantity, String symbol, String currency, Map<String, Double> allPrices) {
		double price = allPrices.getOrDefault(symbol + currency, -1.0);
		if (price > 0.0) {
			return quantity * price;
		} else {
			price = allPrices.getOrDefault(currency + symbol, -1.0);
			if (price > 0.0) {
				return quantity / price;
			}
		}
		return 0.0;
	}

	public boolean waitingForFill(String assetSymbol, Order.Side side) {
		for (var order : pendingOrders.values()) {
			if (order.getAssetsSymbol().equals(assetSymbol)) {
				if (order.getSide() == side) {
					return true;
				}
			} else if (order.getFundsSymbol().equals(assetSymbol)) {
				// If we want to know if there is an open order to buy BTC,
				// and the symbol is "ADABTC", we need to invert the side as
				// we are selling ADA to buy BTC.
				if (side == BUY) {
					if (order.isSell()) {
						return true;
					}
				} else if (side == SELL) {
					if (order.isBuy()) {
						return true;
					}
				}
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
			log.trace("Locking trading on {}", assetSymbol);
			lockedPairs.add(assetSymbol);
		}
	}

	private void unlockTrading(String assetSymbol) {
		synchronized (lockedPairs) {
			if (lockedPairs.contains(assetSymbol)) {
				log.trace("Unlocking trading on {}", assetSymbol);
				lockedPairs.remove(assetSymbol);
			}
		}
	}


	@Override
	public synchronized String toString() {
		StringBuilder out = new StringBuilder();
		Map<String, Balance> positions = account.updateBalances();
		positions.entrySet().stream()
				.filter((e) -> e.getValue().getTotal().doubleValue() > 0.00001)
				.forEach((e) -> out
						.append(e.getKey())
						.append(" = $")
						.append(SymbolPriceDetails.toBigDecimal(2, e.getValue().getTotal().doubleValue()).toPlainString())
						.append('\n'));

		return out.toString();
	}

	private void executeUpdateBalances() {
		Map<String, Balance> updatedBalances = account.updateBalances();
		if (updatedBalances != null && updatedBalances != balances) {
			updatedBalances.keySet().retainAll(configuration.symbols());
			this.balances.clear();
			this.balances.putAll(updatedBalances);

			updatedBalances.values().removeIf(b -> b.getTotal().compareTo(BigDecimal.ZERO) == 0);
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
						orderFinalized(null, order);
						return order;
					case CANCELLED:
						logOrderStatus("Could not create order. ", order);
						orderFinalized(null, order);
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
		orderPreparation.setPrice(priceDetails.priceToBigDecimal(tradingManager.getLatestPrice()));

		if (tradeSide == LONG) {
			if (orderPreparation.isSell()) {
				BigDecimal availableAssets = getPreciseAmount(orderPreparation.getAssetsSymbol());
				if (availableAssets.doubleValue() < quantity) {
					quantity = availableAssets.doubleValue();
				}
			}
		}

		orderPreparation.setQuantity(priceDetails.adjustQuantityScale(quantity));

		OrderBook book = account.getOrderBook(tradingManager.getSymbol(), 0);


		OrderManager orderCreator = configuration.orderManager(tradingManager.getSymbol());
		if (orderCreator != null) {
			orderCreator.prepareOrder(priceDetails, book, orderPreparation, tradingManager.getLatestCandle());
		}

		if (!orderPreparation.isCancelled() && orderPreparation.getTotalOrderAmount().compareTo(priceDetails.getMinimumOrderAmount(orderPreparation.getPrice())) > 0) {
			orderPreparation.setPrice(priceDetails.adjustPriceScale(orderPreparation.getPrice()));
			orderPreparation.setQuantity(priceDetails.adjustQuantityScale(orderPreparation.getQuantity()));

			if (orderPreparation.getTotalOrderAmount().compareTo(priceDetails.getMinimumOrderAmount(orderPreparation.getPrice())) > 0) {
				return orderPreparation;
			}
		}

		return null;
	}

	TradingManager getTradingManagerOf(String symbol) {
		return allTradingManagers.get(symbol);
	}

	public Trader getTraderOf(String symbol) {
		TradingManager tradingManager = getTradingManagerOf(symbol);
		if (tradingManager == null) {
			return null;
		}
		return tradingManager.trader;
	}

	void register(TradingManager tradingManager) {
		this.allTradingManagers.put(tradingManager.getSymbol(), tradingManager);
	}

	public Trader getTraderOfSymbol(String symbol) {
		TradingManager a = allTradingManagers.get(symbol);
		if (a != null) {
			return a.trader;
		}
		return null;
	}

	public Collection<TradingManager> getAllTradingManagers() {
		return allTradingManagers.values();
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
					order.getExecutedQuantity().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getAssetsSymbol(),
					order.getPrice().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getFundsSymbol(),
					TimeInterval.getFormattedDuration(System.currentTimeMillis() - order.getTime()),
					order.getOrderId(),
					order.getQuantity().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getTotalTraded().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getTotalOrderAmount().setScale(8, RoundingMode.FLOOR).toPlainString(),
					order.getFundsSymbol());
		}
	}

	@Override
	public boolean isSimulated() {
		return account.isSimulated();
	}

	private void waitForFill(Order order) {
		pendingOrders.put(order.getOrderId(), order);
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
					Order updated = updateOrder(order);
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

	public Order updateOrder(Order order) {
		OrderManager orderManager = configuration.orderManager(order.getSymbol());
		Order old = order;
		order = account.updateOrderStatus(order);

		Order.Status s = order.getStatus();
		if (order.isFinalized()) {
			logOrderStatus("", order);
			pendingOrders.remove(order.getOrderId());
			orderFinalized(orderManager, order);
			return order;
		} else { // update order status
			pendingOrders.put(order.getOrderId(), order);
		}

		if (old.getExecutedQuantity().compareTo(order.getExecutedQuantity()) != 0) {
			logOrderStatus("", order);
			executeUpdateBalances();
			orderManager.updated(order, traderOf(order), this::resubmit);
		} else {
			logOrderStatus("Unchanged ", order);
			orderManager.unchanged(order, traderOf(order), this::resubmit);
		}

		//order manager could have cancelled the order
		if (order.getStatus() == CANCELLED && pendingOrders.containsKey(order.getOrderId())) {
			cancelOrder(orderManager, order);
		}
		return order;
	}

	private void orderFinalized(OrderManager orderManager, Order order) {
		orderManager = orderManager == null ? configuration.orderManager(order.getSymbol()) : orderManager;
		try {
			executeUpdateBalances();
		} finally {
			Trade trade = null;
			try {
				Trader trader = traderOf(order);
				if (trader != null) {
					trade = trader.tradeOf(order);
				}
				orderManager.finalized(order, trader);
			} finally {
				getTradingManagerOf(order.getSymbol()).notifyOrderFinalized(order, trade);
			}
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

		OrderRequest request = prepareOrder(tradingManager, order.getSide(), order.getTradeSide(), order.getRemainingQuantity().doubleValue(), order);
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
			pendingOrders.remove(order.getOrderId());
			orderFinalized(orderManager, order);
			logOrderStatus("Cancellation via order manager: ", order);
		}
	}

	public synchronized void cancelOrder(Order order) {
		OrderManager orderManager = configuration.orderManager(order.getSymbol());
		if (!order.isFinalized()) {
			Order latestUpdate = pendingOrders.get(order.getOrderId());
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
		for (Map.Entry<String, Order> entry : pendingOrders.entrySet()) {
			Order order = entry.getValue();
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
		this.balances.clear();
		executeUpdateBalances();
		return this;
	}

	public boolean updateOpenOrders(String symbol, Candle candle) {
		if (this.account.updateOpenOrders(symbol, candle)) {
			for (Order order : this.pendingOrders.values()) {
				if (symbol.equals(order.getSymbol())) {
					updateOrder(order);
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
