package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.Signal.*;
import static com.univocity.trader.utils.NewInstances.*;

/**
 * A {@code Trader} is responsible for the lifecycle of a trade (or multiple) and provides all information
 * associated with an open position.
 * It is made available to the user via a {@link StrategyMonitor} to provide information about trades opened by a {@link Strategy}.
 * Once a {@link Strategy} returns a {@link Signal}, the {@link Engine} responsible for the symbol being traded will call
 * {@link Trader#trade(Candle, Signal, Strategy)}, who will then decide whether to buy, sell or ignore the signal, mostly
 * based on its {@link #monitors()} decision.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see StrategyMonitor
 * @see Strategy
 */
public class Trader {
	private static final Logger log = LoggerFactory.getLogger(Trader.class);

	final TradingManager tradingManager;
	private Candle latestCandle;
	private Parameters parameters;

	private StrategyMonitor[] monitors;

	private final Set<Trade> trades = new ConcurrentSkipListSet<>();
	private final Set<Trade> pastTrades = new ConcurrentSkipListSet<>();
	final boolean allowMixedStrategies;
	final OrderListener[] notifications;
	private int pipSize;
	private final List<Trade> stoppedOut = new ArrayList<>();
	private static final AtomicLong id = new AtomicLong(0);

	/**
	 * Creates a new trader for a given symbol. For internal use only.
	 *
	 * @param tradingManager the object responsible for managing the entire trading workflow of a symbol
	 * @param params         optional parameter set used for parameter optimization which is passed on to the
	 *                       {@link StrategyMonitor} instances created by the given monitorProvider
	 * @param allInstances   all known instances of {@link StrategyMonitor} that have been created so far, used
	 *                       to validate no single {@link StrategyMonitor} instance is shared among different
	 *                       {@code Trader} instances.
	 */
	public Trader(TradingManager tradingManager, Parameters params, Set<Object> allInstances) {
		this.parameters = params;
		this.tradingManager = tradingManager;
		this.tradingManager.trader = this;

		this.monitors = createStrategyMonitors(allInstances);
		List<OrderListener> tmp = new ArrayList<>();
		for (StrategyMonitor monitor : monitors) {
			if (monitor instanceof OrderListener) {
				tmp.add((OrderListener) monitor);
			}
		}
		this.notifications = tmp.toArray(new OrderListener[0]);


		boolean allowMixedStrategies = true;
		for (int i = 0; i < this.monitors.length; i++) {
			allowMixedStrategies &= this.monitors[i].allowMixedStrategies();
		}
		this.allowMixedStrategies = allowMixedStrategies;
	}

	/**
	 * Returns the parameters used by the {@link StrategyMonitor} instances in this {@code Trader} instance. Used
	 * mainly to report which parameters are being used in a parameter optimization process.
	 *
	 * @return the parameters tested in the {@link StrategyMonitor} instances of this {@code Trader}.
	 */
	public Parameters parameters() {
		return parameters;
	}


	/**
	 * Evaluates whether to execute a trade given a {@link Signal} emitted by a {@link Strategy} upon processing
	 * the latest {@link Candle} received.
	 *
	 * The actions taken by the trader depend on the signal received:
	 * <ul>
	 * <li>signal = {@code BUY} - an order will be submitted when:
	 * <ol>
	 * <li>there are funds available to purchase that asset (via {@link TradingManager#allocateFunds(Trade.Side)});</li>
	 * <li>none of the associated strategy monitors (from {@link #monitors()} produce
	 * {@link StrategyMonitor#discardBuy(Strategy)};</li>
	 * <li>the {@link OrderRequest} processed by the {@link OrderManager} associated with the symbol is not cancelled
	 * (i.e. {@link OrderRequest#isCancelled()})</li>
	 * </ol>
	 * </li>
	 * <li>signal = {@code SELL}: Sells all assets held for the current symbol, closing any open orders, if:
	 * <ol>
	 * <li>the account has assets available to sell (via {@link TradingManager#hasPosition(Candle, boolean, boolean, boolean)} )</li>
	 * <li>none of the associated strategy monitors (from {@link #monitors()} produce {@code false} upon
	 * invoking {@link StrategyMonitor#allowExit(Trade)};</li>
	 * <li>the {@link OrderRequest} processed by the {@link OrderManager} associated with the symbol is not cancelled
	 * (i.e. {@link OrderRequest#isCancelled()})</li>
	 * </ol>
	 * After the {@link Order} is placed in the {@link Exchange} this {@code Trader} will
	 * holds the trade information and updates statistics on every tick received. If one of the strategy monitors returns
	 * {@code false} for {@link StrategyMonitor#allowMixedStrategies()}, then only signals that come from
	 * the same {@link Strategy} that generated the latest trade will be accepted.</li>
	 * <li>signal = {@code NEUTRAL}: Will simply update the statistics of any open trades.</li>
	 * </ul>
	 * When there is a trade open, regardless of the signal received, all strategy monitors (from
	 * {@link #monitors()} will have their {@link StrategyMonitor#handleStop(Trade, Signal, Strategy)} method called
	 * to determine whether or not to exit the trade. If any one of these calls return
	 * an exit message, the assets will be sold, {@link Trade#stopped()} will evaluate to {@code true} and {@link Trade#exitReason()}
	 * will return the reason for exiting the trade.
	 *
	 * @param candle   the latest candle received for the symbol traded by this {@code Trader}
	 * @param signal   the signal generated by the given strategy after receiving the given candle
	 * @param strategy the strategy that originated the signal
	 *
	 * @return a signal indicating the action taken by this {@code Trader}, i.e. {@code BUY} if it bought assets,
	 */
	public void trade(Candle candle, Signal signal, Strategy strategy) {
		latestCandle = candle;

		removeFinalizedTrades();

		boolean hasPosition = tradingManager.hasPosition(candle, false, true, true);

		if (hasPosition) {
			stoppedOut.clear();
			for (Trade trade : trades) {
				if (trade.tick(candle, signal, strategy) != null) {
					stoppedOut.add(trade);
				}
			}
		}
		if (!stoppedOut.isEmpty()) {
			for (Trade stoppedTrade : stoppedOut) {
				exit(stoppedTrade, candle, strategy, stoppedTrade.exitReason());
			}
		}

		if (signal == BUY) {
			processBuy(candle, strategy);
		} else if (signal == SELL) {
			processSell(candle, strategy);
		}
		stoppedOut.clear();
	}

	private void processBuy(Candle candle, Strategy strategy) {
		boolean isShort = isShort(strategy);
		boolean isLong = isLong(strategy);

		boolean bought = false;

		for (Trade trade : trades) {
			if (stoppedOut.contains(trade)) {
				continue;
			}
			if (isShort && trade.isShort()) {
				bought |= exit(trade, candle, strategy, "Buy signal");
			} else if (isLong && trade.isLong()) {
				bought |= buy(LONG, candle, strategy); //increment position on existing trade
			}
		}

		if (!bought) {
			if (isLong) {
				buy(LONG, candle, strategy); //opens new trade. Can only go long here.
			}
			if (isShort) {
				boolean hasShortPosition = tradingManager.hasPosition(candle, false, false, true);
				if (hasShortPosition) {
					// Buys without having a short trade open. Might happen after starting up
					// with short order in the account. Will generate a warning in the log.
					buy(SHORT, candle, strategy);
				}
			}
		}
	}

	private void processSell(Candle candle, Strategy strategy) {
		boolean isShort = isShort(strategy);
		boolean isLong = isLong(strategy);

		boolean sold = false;
		boolean noLongs = true;
		boolean noShorts = true;

		for (Trade trade : trades) {
			if (stoppedOut.contains(trade)) {
				if (trade.isShort()) {
					noShorts = false;
				}
				if (trade.isLong()) {
					noLongs = false;
				}
				continue;
			}
			if (isShort && trade.isShort()) {
				noShorts = false;
				sold |= sellShort(trade, candle, strategy);
			} else if (isLong && trade.isLong()) {
				noLongs = false;
				sold |= exit(trade, candle, strategy, "Sell signal");
			}
		}

		if (!sold) {
			if (isShort && noShorts) {
				sellShort(null, candle, strategy);
			}
			if (isLong && noLongs) {
				boolean hasLongPosition = tradingManager.hasPosition(candle, false, true, false);
				if (hasLongPosition) {
					// Sell without having a trade open. Might happen after starting up
					// with assets in the account. Will generate a warning in the log.
					Trade trade = Trade.createPlaceholder(-1, this, LONG);
					sellAssets(trade, strategy, "Sell signal");
				}
			}
		}
	}

	private boolean exit(Trade trade, Candle candle, Strategy strategy, String exitReason) {
		if (trade.canExit(strategy)) {
			for (Order order : trade.position()) {
				tradingManager.cancelOrder(order);
			}

			if (!tradingManager.hasPosition(candle, false, trade.isLong(), trade.isShort())) {
				log.trace("Ignoring exit signal of {}: no assets ({})", symbol(), tradingManager.getAssets());
				return false;
			}

			if (trade.isLong()) {
				return sellAssets(trade, strategy, exitReason);
			} else if (trade.isShort()) {
				return closeShort(trade, strategy, exitReason);
			}
		}
		return false;

	}

	StrategyMonitor[] createStrategyMonitors() {
		return createStrategyMonitors(new HashSet<>());
	}

	private StrategyMonitor[] createStrategyMonitors(Set<Object> allInstances) {
		NewInstances<StrategyMonitor> monitorProvider = tradingManager.getAccount().configuration().monitors();
		StrategyMonitor[] out = monitorProvider == null ? new StrategyMonitor[0] : getInstances(tradingManager.getSymbol(), parameters, monitorProvider, "StrategyMonitor", false, allInstances);

		for (int i = 0; i < out.length; i++) {
			out[i].setTrader(this);
		}
		return out;
	}

	boolean isLong(Strategy strategy) {
		return strategy == null || (strategy.tradeSide() == LONG || strategy.tradeSide() == null);
	}

	boolean isShort(Strategy strategy) {
		return tradingManager.canShortSell() && (strategy == null || (strategy.tradeSide() == SHORT || strategy.tradeSide() == null));
	}

	private double prepareTrade(Trade.Side side, Candle candle, Strategy strategy) {
		boolean isLong = side == LONG && isLong(strategy);
		boolean isShort = side == SHORT && isShort(strategy);

		for (int i = 0; i < monitors.length; i++) {
			if ((isLong && monitors[i].discardBuy(strategy)) || (isShort && monitors[i].discardShortSell(strategy))) {
				return -1.0;
			}
		}
		if (!trades.isEmpty()) {
			for (Trade trade : trades) {
				if (trade.tryingToExit() && ((trade.isLong() && isLong) || (trade.isShort() && isShort))) {
					if (isLong) {
						log.trace("Discarding buy of {} @ {}: attempting to sell current {} units", tradingManager.getSymbol(), candle.close, tradingManager.getAssets());
					} else {
						log.trace("Discarding short sell of {} @ {}: attempting to buy more", tradingManager.getSymbol(), candle.close);
					}
					return -1.0;
				}
			}
		}

		if ((isLong && tradingManager.waitingForBuyOrderToFill()) || (isShort && tradingManager.waitingForSellOrderToFill())) {
			tradingManager.cancelStaleOrdersFor(side, this);
			if ((isLong && tradingManager.waitingForBuyOrderToFill()) || (isShort && tradingManager.waitingForSellOrderToFill())) {
				if (isLong) {
					log.trace("Discarding buy of {} @ {}: got buy order waiting to be filled", tradingManager.getSymbol(), candle.close);
				} else {
					log.trace("Discarding short sell of {} @ {}: got sell order waiting to be filled", tradingManager.getSymbol(), candle.close);
				}
				return -1.0;
			}
		}
		if ((isLong && tradingManager.isBuyLocked()) || (isShort && tradingManager.isShortSellLocked())) {
			if (isLong) {
				log.trace("Discarding buy of {} @ {}: purchase order already being processed", tradingManager.getSymbol(), candle.close);
			} else {
				log.trace("Discarding short sell of {} @ {}: sell order already being processed", tradingManager.getSymbol(), candle.close);
			}
			return -1.0;
		}
		double amountToSpend = tradingManager.allocateFunds(side);
		final double minimum = priceDetails().getMinimumOrderAmount(candle.close);
		if (amountToSpend <= minimum) {
			tradingManager.cancelStaleOrdersFor(side, this);
			amountToSpend = tradingManager.allocateFunds(side);
			if (amountToSpend <= minimum) {
				if (tradingManager.exitExistingPositions(tradingManager.assetSymbol, candle, strategy)) {
					tradingManager.updateBalances();
					return amountToSpend;
				} else if (!tradingManager.getAccount().isSimulated()) {
					tradingManager.updateBalances();
					amountToSpend = tradingManager.allocateFunds(side);
					if (amountToSpend <= minimum) {
						if (isLong) {
							log.trace("Discarding buy of {} @ {}: not enough funds to allocate (${})", symbol(), candle.close, tradingManager.getCash());
						} else {
							log.trace("Discarding short selling of {} @ {}: not enough funds to allocate (${})", symbol(), candle.close, tradingManager.getCash());
						}
						return amountToSpend;
					}
				}
			}
		}
		return amountToSpend;
	}

	private boolean sellShort(Trade trade, Candle candle, Strategy strategy) {
		double amountToSpend = prepareTrade(SHORT, candle, strategy);
		if (amountToSpend > 0) {
			Order order = tradingManager.sell(amountToSpend / candle.close, SHORT);
			if (order != null) {
				processOrder(trade, order, strategy, null);
				return true;
			}
			log.trace("Could not short {} @ {}", tradingManager.getSymbol(), candle.close);
		}
		return false;
	}


	private boolean buy(Trade.Side tradeSide, Candle candle, Strategy strategy) {
		Order order = null;
		if (tradeSide == LONG) {
			double amountToSpend = prepareTrade(tradeSide, candle, strategy);
			order = tradingManager.buy(amountToSpend / candle.close, LONG);
		} else if (tradeSide == SHORT) {
			double shortedQuantity = balance().getShorted();
			order = tradingManager.buy(shortedQuantity, SHORT);
		}
		if (order != null) {
			processOrder(null, order, strategy, null);
			return true;
		}
		log.trace("Could not buy {} @ {}", tradingManager.getSymbol(), candle.close);

		return false;
	}

	public double allocateFunds(Trade.Side tradeSide) {
		final double minimum = priceDetails().getMinimumOrderAmount(latestCandle.close);
		double funds = tradingManager.allocateFunds(tradeSide);
		if (funds < minimum) {
			return 0.0;
		}
		return funds;
	}

	private boolean sellAssets(Trade trade, Strategy strategy, String reason) {
		double quantity;
		if (trade != null) {
			quantity = trade.quantityInPosition();
			if (quantity > tradingManager.getAssets() || quantity == 0.0 && trade.isPlaceholder) {
				quantity = tradingManager.getAssets();
			}
		} else {
			quantity = tradingManager.getAssets();
		}


		Order order = tradingManager.sell(quantity, trade.getSide());
		if (order != null) {
			processOrder(trade, order, strategy, reason);
			return true;
		}
		return false;
	}

	private boolean closeShort(Trade trade, Strategy strategy, String exitReason) {
		double reserveFunds = tradingManager.getBalance(fundSymbol()).getMarginReserve(assetSymbol());
		double shortToCover = tradingManager.getBalance(assetSymbol()).getShorted();
		if (shortToCover > 0) {
			if (shortToCover * trade.lastClosingPrice() > reserveFunds) {
				log.warn("Not enough funds in margin reserve to cover short of {} {} @ {} {} per unit. Reserve: {}, required {} {}", assetSymbol(), shortToCover, trade.lastClosingPrice(), fundSymbol(), reserveFunds, shortToCover * trade.lastClosingPrice(), fundSymbol());
			}

			Order order = tradingManager.buy(shortToCover, SHORT);
			if (order != null) {
				processOrder(trade, order, strategy, exitReason);
				return true;
			}
		}
		return false;
	}


	private void cancelOpenBuyOrders(Strategy strategy) {
		for (Trade trade : trades) {
			cancelOpenBuyOrders(trade, strategy);
		}
	}

	private void cancelOpenBuyOrders(Trade trade, Strategy strategy) {
		if (trade.canExit(strategy)) {
			for (Order order : trade.position()) {
				tradingManager.cancelOrder(order);
			}
		}
	}

	/**
	 * Returns the estimated total funds in the reference currency (given by {@link com.univocity.trader.config.AccountConfiguration#referenceCurrency()})
	 *
	 * @return the estimate net worth of the account.
	 */
	public double holdings() {
		return tradingManager.getTotalFundsInReferenceCurrency();
	}


	/**
	 * Return the latest candle received by this {@code trader}
	 *
	 * @return the most recent tick received for the symbol being traded.
	 */
	public Candle latestCandle() {
		return latestCandle;
	}

	public void notifySimulationEnd() {
		tradingManager.notifySimulationEnd();
	}

	public void liquidateOpenPositions() {
		for (Trade t : trades) {
			if (!t.isFinalized()) {
				if (t.isLong()) {
					if (!sellAssets(t, null, "Liquidate position")) {
						log.warn("Could not liquidate open trade: " + t);
					} else {
						t.liquidate();
					}
				} else if (t.isShort()) {
					if (!closeShort(t, null, "Liquidate position")) {
						log.warn("Could not liquidate open trade: " + t);
					} else {
						t.liquidate();
					}
				}
			}
		}
	}

	public Order submitOrder(Order.Type type, Order.Side side, Trade.Side tradeSide, double quantity) {
		return tradingManager.getAccount().submitOrder(this, quantity, side, tradeSide, type);
	}

	void processOrder(Trade trade, Order order, Strategy strategy, String reason) {
		if (order == null || (order.isCancelled() && order.getFillPct() == 0.0)) {
			return;
		}
		try {
			try {
				if (order.isBuy()) {
					trade = processBuyOrder(order, strategy, reason);
				} else if (order.isSell()) {
					trade = processSellOrder(trade, order, strategy, reason);
				}

				if (trade != null) {
					trades.add(trade);
				}
			} finally {
				tradingManager.updateBalances();
			}
		} catch (Exception e) {
			log.error("Error processing " + order.getSide() + " order: " + order, e);
		} finally {
			tradingManager.notifyOrderSubmitted(order, trade);
		}
	}

	private void attachOrdersToTrade(Trade trade, Order parent) {
		if (parent.getAttachments() != null) {
			if (trade == null) {
				throw new IllegalStateException("Can't process buy order without a valid trade instance");
			}

			for (Order attached : parent.getAttachments()) {
				trade.decreasePosition(attached, "Stop order");
			}
		}
	}

	private Trade processBuyOrder(Order order, Strategy strategy, String reason) {
		Trade trade = null;
		for (Trade t : trades) {
			if (!t.isFinalized() && t.getSide() == order.getTradeSide()) {
				trade = t;
				break;
			}
		}

		if (trade == null) {
			if (order.isLong()) {
				trade = new Trade(id.incrementAndGet(), order, this, strategy);
				trades.add(trade);
			} else if (order.isShort()) {
				trade = Trade.createPlaceholder(-1, this, order.getTradeSide());
				trade.decreasePosition(order, "Exit short position");
				log.warn("Received a short-covering buy order without an open trade: " + order);
			}
		} else {
			if (order.isLong()) {
				if (!trade.increasePosition(order)) {
					trade = new Trade(id.incrementAndGet(), order, this, strategy);
					trades.add(trade);
				}
			} else if (order.isShort()) {
				trade.decreasePosition(order, reason);
			}
		}

		attachOrdersToTrade(trade, order);
		return trade;
	}


	private Trade processSellOrder(Trade trade, Order order, Strategy strategy, String reason) {
		if (trade == null) {
			for (Trade t : trades) {
				if (!t.isFinalized() && t.canExit(strategy)) {
					trade = t;
					break;
				}
			}
		}

		if (trade != null) {
			if (trade.isShort()) {
				if (!trade.increasePosition(order)) {
					trade = new Trade(id.incrementAndGet(), order, this, strategy);
					trades.add(trade);
				}
			} else {
				trade.decreasePosition(order, reason);
			}
		} else {
			if (order.isShort()) {
				trade = new Trade(id.incrementAndGet(), order, this, strategy);
				trades.add(trade);
			} else {
				trade = Trade.createPlaceholder(-1, this, order.getTradeSide());
				trade.decreasePosition(order, "Exit long position");
				//could be manual
				log.warn("Received a sell order without an open trade: " + order);
			}
		}
		attachOrdersToTrade(trade, order);
		return trade;
	}

	/**
	 * Tries to exit a current trade to immediately buy into another instrument. In cases where it's supported, such as currencies and crypto, a "direct" switch
	 * will be executed to save trading fees;
	 * i.e. if there's an open position on BTCUSDT, and the exit symbol is ETH, a single SELL order of symbol BTCETH will be executed, selling BTC to open a
	 * position in ETH. If no compatible trading
	 * symbols exists, or the market operates just with stocks, the current position will be sold in order to make funds available for buying into the next
	 * instrument. Using the previous example, BTC
	 * would be sold back into USDT, and another BUY order would be made using the USDT funds to buy ETH. If any call {@link
	 * StrategyMonitor#allowTradeSwitch(Trade, String, Candle, String)} evaluates to
	 * {@code false}, the "switch" operation will be cancelled, otherwise the current open position will be closed to release funds for the trader to BUY into
	 * the exitSymbol.
	 *
	 * @param exitSymbol   the new instrument to be bought into using the funds allocated by the current open order (in {@link #symbol()}.
	 * @param candle       the latest candle of the exitSymbol that's been received from the exchange.
	 * @param candleTicker the ticker of the received candle (not the same as {@link #symbol()}).
	 * @param strategy     the strategy that identified an opportunity to open a position on the given exitSymbol.
	 *
	 * @return a flag indicating whether an order for a direct switch was opened. If {@code true}, the trader won't try to create a BUY order for the given
	 * exitSymbol. When {@code false} the trader
	 * will try to buy into the exitSymbol regardless of whether the current position was closed or not.
	 */
	boolean switchTo(String exitSymbol, Candle candle, String candleTicker, Strategy strategy) {
		try {
			if (exitSymbol.equals(tradingManager.fundSymbol) || exitSymbol.equals(tradingManager.assetSymbol)) {
				return false;
			}

			for (Trade trade : trades) {
				if (!trade.isFinalized() && trade.allowTradeSwitch(exitSymbol, candle, candleTicker)) {
					if (exit(trade, trade.latestCandle(), strategy, "exit to buy " + exitSymbol)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not exit trade from " + tradingManager.assetSymbol + " to " + exitSymbol, e);
		}
		// could not sell directly to exit symbol. Might have sold or did not sell at all. In either case caller will try to BUY.
		return false;
	}

	/**
	 * Returns the last closing price of the symbol being traded by this {@code Trader}
	 *
	 * @return the {@link Candle#close} of the latest candle (returned via {@link #latestCandle()}.
	 */
	public double lastClosingPrice() {
		if (latestCandle == null) {
			return 0.0;
		}
		return latestCandle.close;
	}

	public double assetQuantity() {
		return tradingManager.getAssets();
	}

	public String symbol() {
		return tradingManager.getSymbol();
	}

	public String assetSymbol() {
		return tradingManager.getAssetSymbol();
	}

	public String fundSymbol() {
		return tradingManager.getFundSymbol();
	}

	public String referenceCurrencySymbol() {
		return tradingManager.getReferenceCurrencySymbol();
	}

	public TradingFees tradingFees() {
		return tradingManager.getTradingFees();
	}

	public double breakEvenChange(double amount) {
		return tradingManager.getTradingFees().getBreakEvenChange(amount);
	}

	public double breakEvenAmount(double amount) {
		return tradingManager.getTradingFees().getBreakEvenAmount(amount);
	}

	public double totalFundsInReferenceCurrency() {
		return tradingManager.getTotalFundsInReferenceCurrency();
	}

	public double totalFundsIn(String symbol) {
		return tradingManager.getTotalFundsIn(symbol);
	}

	public Balance balance(String symbol) {
		return tradingManager.getBalance(symbol).clone();
	}

	public Balance balance() {
		return tradingManager.getBalance(referenceCurrencySymbol()).clone();
	}

	public Balance balanceOf(String symbol) {
		return tradingManager.getBalance(symbol).clone();
	}

	public SymbolPriceDetails priceDetails() {
		return tradingManager.getPriceDetails();
	}

	public SymbolPriceDetails referencePriceDetails() {
		return tradingManager.getReferencePriceDetails();
	}

	public String toString() {
		return "Trader{" + symbol() + "}";
	}

	void orderFinalized(Order order) {
		Trade trade = tradeOf(order);
		if (trade != null) {
			trade.orderFinalized(order);
		}
	}

	void removeFinalizedTrades() {
		for (Trade trade : trades) {
			if (trade.isFinalized()) {
				pastTrades.add(trade);
				trades.remove(trade);
			}
		}
	}

	public Trade tradeOf(Order order) {
		for (Trade trade : trades) {
			if (trade.hasOrder(order)) {
				return trade;
			}
		}
		for (Trade trade : pastTrades) {
			if (trade.hasOrder(order)) {
				return trade;
			}
		}
		return null;
	}

	public Strategy strategyOf(Order order) {
		if (!allowMixedStrategies) {
			for (Trade trade : trades) {
				if (trade.hasOrder(order)) {
					return trade.openingStrategy();
				}
			}
		}
		return null;
	}

	/**
	 * Returns all {@link StrategyMonitor} instances built in the constructor of this class, which will be used by
	 * an {@link Engine} that processes candles for the symbol traded by this {@code Trader}
	 *
	 * @return all strategy monitors used by this {@code Trader}.
	 */
	public StrategyMonitor[] monitors() {
		return monitors;
	}

	public Set<Trade> trades() {
		return new TreeSet<>(trades);
	}

	public int pipSize() {
		if (pipSize == 0) {
			pipSize = tradingManager.pipSize();
		}

		if (pipSize == 0) {
			pipSize = Utils.countDecimals(lastClosingPrice());
		}

		return pipSize;
	}
}
