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
import java.util.stream.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.config.Allocation.*;

public final class TradingManager {

	private static final Logger log = LoggerFactory.getLogger(TradingManager.class);

	private static final long FIFTEEN_SECONDS = TimeInterval.seconds(15).ms;

	private final String symbol;
	final String assetSymbol;
	final String fundSymbol;
	private final AccountManager tradingAccount;
	protected Trader trader;
	private Exchange<?, ?> exchange;
	private final OrderListener[] notifications;
	private final Client client;
	private OrderExecutionToEmail emailNotifier;
	private final SymbolPriceDetails priceDetails;
	private final SymbolPriceDetails referencePriceDetails;
	private final AbstractTradingGroup<?> configuration;
	final Map<String, Trader> traders = new ConcurrentHashMap<>();
	final Map<String, TradingManager> allTradingManagers = new ConcurrentHashMap<>();
	TradingManager[] tradingManagers;
	final Map<Integer, long[]> fundAllocationCache = new ConcurrentHashMap<>();
	final OrderSet pendingOrders = new OrderSet();
	final OrderSet orderUpdates = new OrderSet();
	final Context context;

	public TradingManager(AbstractTradingGroup<?> configuration, Exchange exchange, SymbolPriceDetails priceDetails, AccountManager account, String assetSymbol, String fundSymbol, Parameters params) {
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
		this.context = new Context(this, params);
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
		return trader.latestCandle();
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

	public Map<String, double[]> getAllPrices() {
		return exchange.getLatestPrices();
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
		return positionValue > minimum && positionValue > minimumInvestmentAmountPerTrade();
	}

	double minimumInvestmentAmountPerTrade() {
		return configuration.minimumInvestmentAmountPerTrade(assetSymbol);
	}

	public final Order buy(double quantity, Trade.Side tradeSide) {
		return buy(assetSymbol, fundSymbol, tradeSide, quantity);
	}

//	public boolean switchTo(String ticker, Signal trade, String exitSymbol) {
//		String targetSymbol = exitSymbol + fundSymbol;
//		double targetUnitPrice = getLatestPrice(exitSymbol, fundSymbol);
//		if (targetUnitPrice <= 0.0) {
//			return false;
//		}
//
//		final Trader purchaseTrader = getTraderOf(targetSymbol);
//		if (trader != null) {
//			double quantityToSell = tradingAccount.allocateFunds(exitSymbol, assetSymbol);
//			double saleUnitPrice = trader.getLastClosingPrice();
//			double saleAmount = quantityToSell * saleUnitPrice;
//			double quantityToBuy = saleAmount / targetUnitPrice;
//
//			trader.setExitReason("Switching from " + ticker + " to " + targetSymbol);
//
//			if (trade == SELL) {
//				return processOrder(trader, tradingAccount.sell(assetSymbol, exitSymbol, quantityToSell));
//			} else {
//				return processOrder(purchaseTrader, tradingAccount.buy(exitSymbol, assetSymbol, quantityToBuy));
//			}
//		}
//		return false;
//	}

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

	public double allocateFunds(Trade.Side tradeSide) {
		return allocateFunds(assetSymbol, tradeSide);
	}

	public double getTotalFundsInReferenceCurrency() {
		return tradingAccount.getTotalFundsInReferenceCurrency(getAllPrices());
	}

	public double getTotalFundsIn(String symbol) {
		return tradingAccount.getTotalFundsIn(symbol, getAllPrices());
	}

	public boolean exitExistingPositions(String exitSymbol, Candle c, Strategy strategy) {
		boolean exited = false;
		TradingManager[] managers = getAllTradingManagers();
		for (int i = 0; i < managers.length; i++) {
			TradingManager manager = managers[i];
			if (manager != this && manager.hasPosition(c, false, true, true) && manager.trader.switchTo(exitSymbol, c, manager.symbol)) {
				exited = true;
				break;
			}
		}
		return exited;
	}

	public boolean waitingForBuyOrderToFill(Trade.Side tradeSide) {
		return waitingForFill(assetSymbol, BUY, tradeSide);
	}

	public boolean waitingForSellOrderToFill(Trade.Side tradeSide) {
		return waitingForFill(assetSymbol, SELL, tradeSide);
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

	public void cancelStaleOrdersFor(Trade.Side side, Trader trader) {
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

	private Order cancelOrder(OrderManager orderManager, Order order) {
		try {
			order.cancel();
			tradingAccount.cancel(order);
		} catch (Exception e) {
			log.error("Failed to execute cancellation of order '" + order + "' on exchange", e);
		} finally {
			removePendingOrder(order);
			order = tradingAccount.updateOrderStatus(order);
			orderFinalized(orderManager, order);
			logOrderStatus("Cancellation via order manager: ", order);
		}
		return order;
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
		if (!tradingAccount.isSimulated()) {
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

		if (update.getExecutedQuantity() != order.getExecutedQuantity() || (tradingAccount.isSimulated() && update instanceof DefaultOrder && ((DefaultOrder) update).hasPartialFillDetails())) {
			logOrderStatus("Order updated. ", update);
			tradingAccount.executeUpdateBalances();
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


	public Order cancelOrder(Order order) {
		return cancelOrder(configuration.orderManager(order.getSymbol()), order);
	}

	public Order submitOrder(Trader trader, double quantity, Order.Side side, Trade.Side tradeSide, Order.Type type) {
		OrderRequest request = prepareOrder(side, tradeSide, quantity, null);
		Order order = tradingAccount.executeOrder(request);
		trader.processOrder(order);
		return order;
	}

	public Order buy(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity) {
		if (tradingAccount.lockTrading(assetSymbol)) {
			try {
				String symbol = assetSymbol + fundSymbol;
				TradingManager tradingManager = getTradingManagerOf(symbol);
				if (tradingManager == null) {
					throw new IllegalStateException("Unable to buy " + quantity + " units of unknown symbol: " + symbol);
				}
				if (tradeSide == SHORT) {
					OrderRequest orderPreparation = prepareOrder(BUY, SHORT, quantity, null);
					return tradingAccount.executeOrder(orderPreparation);
				}
				double maxSpend = tradingManager.allocateFunds(assetSymbol, tradeSide);
				if (maxSpend > 0) {
					maxSpend = getTradingFees().takeFee(maxSpend, Order.Type.MARKET, BUY);
					double expectedCost = quantity * tradingManager.getLatestPrice();
					if (expectedCost > maxSpend) {
						quantity = quantity * (maxSpend / expectedCost);
					}
					quantity = quantity * 0.9999;
					OrderRequest orderPreparation = prepareOrder(BUY, tradeSide, quantity, null);
					return tradingAccount.executeOrder(orderPreparation);
				}
			} finally {
				tradingAccount.unlockTrading(assetSymbol);
			}
		}
		return null;
	}

	public Order sell(String assetSymbol, String fundSymbol, Trade.Side tradeSide, double quantity) {
		String symbol = assetSymbol + fundSymbol;
		TradingManager tradingManager = getTradingManagerOf(symbol);
		if (tradingManager == null) {
			throw new IllegalStateException("Unable to sell " + quantity + " units of unknown symbol: " + symbol);
		}
		OrderRequest orderPreparation = prepareOrder(SELL, tradeSide, quantity, null);
		return tradingAccount.executeOrder(orderPreparation);
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

		Trade trade = order.getTrade();
		Context context = trader.context;

		updateOpenOrders();

		OrderRequest request = prepareOrder(order.getSide(), order.getTradeSide(), order.getRemainingQuantity(), order);
		order = tradingAccount.executeOrder(request);

		Strategy strategy = getTrader().strategyOf(order);

		context.trade = trade;
		context.strategy = strategy;
		context.exitReason = trade == null ? "Order resubmission" : "Order resubmission: " + trade.exitReason();
		trader.processOrder(order);
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


		OrderManager orderCreator = configuration.orderManager(getSymbol());
		if (orderCreator != null) {
			orderCreator.prepareOrder(book, orderPreparation, context);
		}
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
		notifyOrderSubmitted(order, trade, this.notifications);
		notifyOrderSubmitted(order, trade, trader.notifications);
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


	public final double allocateFunds(String assetSymbol, Trade.Side tradeSide) {
		return tradingAccount.allocateFunds(assetSymbol, getReferenceCurrencySymbol(), tradeSide, this);
	}

	double executeAllocateFunds(String assetSymbol, String fundSymbol, Trade.Side tradeSide) {
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

		double allocated = tradingAccount.getAmount(assetSymbol);
		double shorted = tradingAccount.getShortedAmount(assetSymbol);
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

	private Trader traderOf(Order order) {
		return getTraderOfSymbol(order.getSymbol());
	}

	void orderFinalized(OrderManager orderManager, Order order) {
		orderManager = orderManager == null ? configuration.orderManager(order.getSymbol()) : orderManager;
		try {
			tradingAccount.executeUpdateBalances();
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

	public void removePendingOrder(Order order) {
		pendingOrders.remove(order);
		synchronized (orderUpdates) {
			orderUpdates.remove(order);
		}
	}

	public void waitForFill(Order order) {
		pendingOrders.addOrReplace(order);
		if (tradingAccount.isSimulated()) {
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
						o = tradingAccount.updateOrderStatus(o);
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

	public void updateOpenOrders() {
		synchronized (orderUpdates) {
			for (int i = orderUpdates.i - 1; i >= 0; i--) {
				Order order = orderUpdates.elements[i];
				if (symbol.equals(order.getSymbol())) {
					updateOrder(order);
				}
			}
		}
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
		return tradingAccount.queryBalance(assetSymbol, Balance::isTradingLocked);
	}

	public boolean isBuyLocked(String assetSymbol) {
		return isTradingLocked(assetSymbol) || waitingForFill(assetSymbol, BUY, LONG);
	}

	public boolean isShortSellLocked(String assetSymbol) {
		return (isTradingLocked(assetSymbol) || waitingForFill(assetSymbol, SELL, SHORT));
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
}
