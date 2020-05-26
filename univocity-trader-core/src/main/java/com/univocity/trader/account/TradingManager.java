package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.config.Allocation.*;
import static com.univocity.trader.utils.AbstractNewInstances.*;

public final class TradingManager {

	private static final Logger log = LoggerFactory.getLogger(TradingManager.class);

	private static final long FIFTEEN_SECONDS = TimeInterval.seconds(15).ms;

	final String symbol;
	final String assetSymbol;
	final String fundSymbol;
	private final AccountManager tradingAccount;
	protected final Trader trader;
	private Exchange<?, ?> exchange;
	private final OrderListener[] notifications;
	private final Client client;
	private OrderExecutionToEmail emailNotifier;
	private final SymbolPriceDetails priceDetails;
	private final SymbolPriceDetails referencePriceDetails;
	private final AbstractTradingGroup<?> configuration;
	final OrderManager orderManager;
	final OrderTracker orderTracker;

	final Map<Integer, long[]> fundAllocationCache = new ConcurrentHashMap<>();

	final Context context;

	TradingManager(AbstractTradingGroup<?> configuration, Exchange exchange, SymbolPriceDetails priceDetails, AccountManager account, String assetSymbol, String fundSymbol, Parameters params, Set<Object> allInstances) {
		if (exchange == null) {
			throw new IllegalArgumentException("Exchange implementation cannot be null");
		}
		if (account == null) {
			throw new IllegalArgumentException("Account manager cannot be null");
		}
		if (StringUtils.isBlank(assetSymbol)) {
			throw new IllegalArgumentException("Symbol of instrument to buy cannot be blank (examples: 'MSFT', 'BTC', 'EUR')");
		}
		if (StringUtils.isBlank(fundSymbol)) {
			throw new IllegalArgumentException("Currency cannot be blank (examples: 'USD', 'EUR', 'USDT', 'ETH')");
		}
		if (priceDetails == null) {
			priceDetails = new SymbolPriceDetails(exchange, account.getReferenceCurrencySymbol());
		}

		this.exchange = exchange;
		this.client = account.getClient();
		this.assetSymbol = assetSymbol.intern();
		this.fundSymbol = fundSymbol.intern();
		this.symbol = (assetSymbol + fundSymbol).intern();

		Instances<OrderListener> listenerProvider = configuration.listeners();
		this.notifications = listenerProvider != null ? listenerProvider.create(symbol, params) : new OrderListener[0];
		this.tradingAccount = client.getAccountManager();
		this.emailNotifier = getEmailNotifier();

		this.priceDetails = priceDetails.switchToSymbol(symbol);
		this.referencePriceDetails = priceDetails.switchToSymbol(getReferenceCurrencySymbol());
		this.configuration = configuration;
		this.orderManager = configuration.orderManager(symbol);
		this.context = new Context(this, params);

		StrategyMonitor[] monitors = createStrategyMonitors(allInstances, params);

		this.trader = new Trader(this, monitors);
		this.orderTracker = new OrderTracker(this);

		for (int i = 0; i < monitors.length; i++) {
			monitors[i].setContext(this.context);
		}
	}

	private StrategyMonitor[] createStrategyMonitors(Set<Object> allInstances, Parameters parameters) {
		NewInstances<StrategyMonitor> monitorProvider = monitors();
		StrategyMonitor[] out = monitorProvider == null ? new StrategyMonitor[0] : getInstances(getSymbol(), parameters, monitorProvider, "StrategyMonitor", false, allInstances);
		return out;
	}


	public SymbolPriceDetails getPriceDetails() {
		return priceDetails;
	}

	public SymbolPriceDetails getReferencePriceDetails() {
		return referencePriceDetails;
	}

	public String getFundSymbol() {
		return fundSymbol;
	}

	public String getAssetSymbol() {
		return assetSymbol;
	}

	public final double getLatestPrice() {
		return getLatestPrice(assetSymbol, fundSymbol);
	}

	public final Candle getLatestCandle() {
		return context.latestCandle();
	}

	public final double getLatestPrice(String assetSymbol, String fundSymbol) {
		Candle lastCandle = getLatestCandle();
		if (lastCandle != null && (tradingAccount.isSimulated() || (System.currentTimeMillis() - lastCandle.closeTime) < FIFTEEN_SECONDS)) {
			return lastCandle.close;
		}
		return exchange.getLatestPrice(assetSymbol, fundSymbol);
	}

	public final String getSymbol() {
		return symbol;
	}

	public boolean hasPosition(Candle c, boolean includeLocked, boolean includeLong, boolean includeShort) {
		double minimum = getPriceDetails().getMinimumOrderAmount(c.close);

		double assets = 0.0;

		if (includeLong) {
			assets = (includeLocked ? getTotalAssets() : getAssets());
		}

		if (includeShort) {
			assets += getShortedAssets();
		}

		double positionValue = assets * c.close;

		if (includeShort && !includeLong) {
			return positionValue > EFFECTIVELY_ZERO;
		}

		if(positionValue > minimumInvestmentAmountPerTrade() / 2.0){
			return true; //ensure trade that is near or at the minimum amount is not exited without a stop/exit signal.
		}

		return positionValue > minimum && positionValue > minimumInvestmentAmountPerTrade();
	}

	double minimumInvestmentAmountPerTrade() {
		return configuration.minimumInvestmentAmountPerTrade(assetSymbol);
	}

	public Order sell(double quantity, Trade.Side tradeSide) {
		if (!trader.liquidating && quantity * getLatestPrice() < minimumInvestmentAmountPerTrade()) {
			return null;
		}
		return sell(assetSymbol, fundSymbol, tradeSide, quantity);
	}

	public Order sell(Trade.Side tradeSide) {
		return sell(getAssets(), tradeSide);
	}

	public double getAssets() {
		return tradingAccount.getAmount(assetSymbol);
	}

	public double getShortedAssets() {
		return tradingAccount.getShortedAmount(assetSymbol);
	}

	public double getTotalAssets() {
		return tradingAccount.getBalance(assetSymbol, Balance::getTotal);
	}

	public double getCash() {
		return tradingAccount.getAmount(fundSymbol);
	}

	public double getTotalFundsInReferenceCurrency() {
		return tradingAccount.getTotalFundsInReferenceCurrency();
	}

	public double getTotalFundsIn(String symbol) {
		return tradingAccount.getTotalFundsIn(symbol);
	}

	public boolean exitExistingPositions(String exitSymbol, Candle c) {
		return tradingAccount.getFromFirstTradingManager(manager -> {
			if (manager != this && manager.hasPosition(c, false, true, true) && manager.trader.switchTo(exitSymbol, c, manager.symbol)) {
				return manager;
			}
			return null;
		}) != null;
	}

	public boolean waitingForBuyOrderToFill(Trade.Side tradeSide) {
		return orderTracker.waitingForFill(assetSymbol, BUY, tradeSide);
	}

	public boolean waitingForSellOrderToFill(Trade.Side tradeSide) {
		return orderTracker.waitingForFill(assetSymbol, SELL, tradeSide);
	}

	public final void updateBalances() {
		tradingAccount.updateBalances();
	}

	public String getReferenceCurrencySymbol() {
		return tradingAccount.getReferenceCurrencySymbol();
	}

	public final Trader getTrader() {
		return this.trader;
	}

	public boolean isBuyLocked() {
		return isBuyLocked(assetSymbol);
	}

	public boolean isShortSellLocked() {
		return isShortSellLocked(assetSymbol);
	}

	public AccountManager getAccount() {
		return tradingAccount;
	}

	public Order submitOrder(Trader trader, double quantity, Order.Side side, Trade.Side tradeSide, Order.Type type) {
		OrderRequest request = prepareOrder(side, tradeSide, quantity, null);
		request.setType(type);
		Order order = executeOrder(request);
		trader.processOrder(order);
		return order;
	}

	public Order buy(Trade.Side tradeSide, double quantity) {
		if (tradingAccount.lockTrading(assetSymbol)) {
			try {
				if (tradeSide == SHORT) {
					OrderRequest orderPreparation = prepareOrder(BUY, SHORT, quantity, null);
					return executeOrder(orderPreparation);
				}
				double maxSpend = allocateFunds(tradeSide);
				if (maxSpend > 0) {
					maxSpend = getTradingFees().takeFee(maxSpend, Order.Type.MARKET, BUY);
					double expectedCost = quantity * getLatestPrice();
					if (expectedCost > maxSpend) {
						quantity = quantity * (maxSpend / expectedCost);
					}
					quantity = quantity * 0.9999;
					OrderRequest orderPreparation = prepareOrder(BUY, tradeSide, quantity, null);
					return executeOrder(orderPreparation);
				}
			} finally {
				tradingAccount.unlockTrading(assetSymbol);
			}
		}
		return null;
	}

	public Order sell(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity) {
		OrderRequest orderPreparation = prepareOrder(SELL, tradeSide, quantity, null);
		return executeOrder(orderPreparation);
	}

	void resubmit(Order order) {
		if (order == null) {
			throw new IllegalArgumentException("Order for resubmission cannot be null");
		}

		if (order.getFillPct() > 98.0) {
			//ignore orders 98% filled.
			return;
		}

		orderTracker.cancelOrder(order);

		Trade trade = order.getTrade();
		Context context = trader.context;

		orderTracker.updateOpenOrders();

		OrderRequest request = prepareOrder(order.getSide(), order.getTradeSide(), order.getRemainingQuantity(), order);
		order = executeOrder(request);

		Strategy strategy = getTrader().strategyOf(order);

		context.trade = trade;
		context.strategy = strategy;
		context.exitReason = trade == null ? "Order resubmission" : "Order resubmission: " + trade.exitReason();
		trader.processOrder(order);
	}

	private Order executeOrder(OrderRequest request){
		return tradingAccount.executeOrder(request);
	}

	private OrderRequest prepareOrder(Order.Side side, Trade.Side tradeSide, double quantity, Order resubmissionFrom) {
		long time = getLatestCandle().closeTime;
		OrderRequest orderPreparation = new OrderRequest(getAssetSymbol(), getFundSymbol(), side, tradeSide, time, resubmissionFrom);
		orderPreparation.setPrice(getLatestPrice());

		if (tradeSide == LONG) {
			if (orderPreparation.isSell()) {
				double availableAssets = tradingAccount.getAmount(orderPreparation.getAssetsSymbol());
				if (availableAssets < quantity) {
					quantity = availableAssets;
				}
			}
		}

		orderPreparation.setQuantity(quantity);

		OrderBook book = tradingAccount.getOrderBook(getSymbol(), 0);


		orderManager.prepareOrder(book, orderPreparation, context);
		SymbolPriceDetails priceDetails = context.priceDetails();
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

	public TradingFees getTradingFees() {
		return tradingAccount.getTradingFees();
	}

	public void sendBalanceEmail(String title, Client client) {
		getEmailNotifier().sendBalanceEmail(title, client);
	}

	public OrderExecutionToEmail getEmailNotifier() {
		if (emailNotifier == null) {
			for (int i = 0; i < notifications.length; i++) {
				if (notifications[i] instanceof OrderExecutionToEmail) {
					emailNotifier = (OrderExecutionToEmail) notifications[i];
					break;
				}
			}
			if (emailNotifier == null) {
				emailNotifier = new OrderExecutionToEmail();
			}
			emailNotifier.initialize(this);
		}
		return emailNotifier;
	}

	void notifyOrderSubmitted(Order order, Trade trade) {
		try {
			notifyOrderSubmitted(order, trade, this.notifications);
			notifyOrderSubmitted(order, trade, trader.notifications);
		} finally {
			orderTracker.initiateOrderMonitoring(order);
		}
	}

	private Trade getTradeForOrder(Trade trade, Order order) {
		if (trade != null) {
			return trade;
		}
		return Trade.createPlaceholder(-1, getTrader(), order.getTradeSide());
	}

	private void notifyOrderSubmitted(Order order, Trade trade, OrderListener[] notifications) {
		for (int i = 0; i < notifications.length; i++) {
			try {
				trade = getTradeForOrder(trade, order);
				notifications[i].orderSubmitted(order, trade, client);
				if (order.getAttachments() != null) {
					for (Order attached : order.getAttachments()) {
						notifications[i].orderSubmitted(attached, trade, client);
					}
				}
			} catch (Exception e) {
				log.error("Error sending orderSubmitted notification for order: " + order, e);
			}
		}
	}

	private void notifyOrderFinalized(Order order, OrderListener[] notifications) {
		for (int i = 0; i < notifications.length; i++) {
			try {
				notifications[i].orderFinalized(order, order.getTrade(), client);
			} catch (Exception e) {
				log.error("Error sending orderFinalized notification for order: " + order, e);
			}
		}
	}

	void notifyOrderFinalized(Order order) {
		if (trader.orderFinalized(order)) {
			notifyOrderFinalized(order, this.notifications);
			notifyOrderFinalized(order, trader.notifications);
		}
	}

	void notifySimulationEnd() {
		notifySimulationEnd(this.notifications);
		notifySimulationEnd(trader.notifications);
		SimulatedAccountManager account = (SimulatedAccountManager) getAccount();
		account.balanceUpdateCounts.clear();
		account.notifySimulationEnd();
	}

	private void notifySimulationEnd(OrderListener[] notifications) {
		for (int i = 0; i < notifications.length; i++) {
			try {
				notifications[i].simulationEnded(trader, client);
			} catch (Exception e) {
				log.error("Error sending onSimulationEnd notification", e);
			}
		}
	}

	public int pipSize() {
		return priceDetails.pipSize();
	}

	public boolean canShortSell() {
		return tradingAccount.canShortSell();
	}

	public Map<String, Balance> getBalanceSnapshot() {
		return tradingAccount.getBalanceSnapshot();
	}

	public final double allocateFunds(Trade.Side tradeSide) {
		return tradingAccount.allocateFunds(assetSymbol, getReferenceCurrencySymbol(), tradeSide, this);
	}

	double executeAllocateFunds(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
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

		double allocated = tradingAccount.getAmount(assetSymbol);
		double shorted = tradingAccount.getShortedAmount(assetSymbol);
		double unitPrice = getLatestPrice();
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

		final double freeAmount = tradingAccount.getAmount(fundSymbol);
		double out = Math.min(available, freeAmount);

		if (tradeSide == SHORT) {
			out *= tradingAccount.marginReserveFactorPct;
			out = Math.min(out, freeAmount);
			out = Math.min(out, maxAmountPerTrade);
		}
		if (out < minimumInvestment) {
			return 0.0;
		}
		return out;
	}

	private boolean isTradingLocked(String assetSymbol) {
		return tradingAccount.queryBalance(assetSymbol, Balance::isTradingLocked);
	}

	public boolean isBuyLocked(String assetSymbol) {
		return isTradingLocked(assetSymbol) || waitingForBuyOrderToFill(LONG);
	}

	public boolean isShortSellLocked(String assetSymbol) {
		return (isTradingLocked(assetSymbol) || waitingForSellOrderToFill(SHORT));
	}

	static void logOrderStatus(String msg, Order order) {
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

	public NewInstances<Strategy> strategies() {
		return configuration.strategies();
	}

	public NewInstances<StrategyMonitor> monitors() {
		return configuration.monitors();
	}

	void cancelOrder(Order order) {
		orderTracker.cancelOrder(order);
	}

	public void cancelStaleOrdersFor(Trade.Side side, Trader trader) {
		orderTracker.cancelStaleOrdersFor(side, trader);
	}

	public void updateOpenOrders() {
		orderTracker.updateOpenOrders();
	}

	public void waitForFill(Order order){
		orderTracker.waitForFill(order);
	}
}
