package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.Signal.*;

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
public final class Trader {
	private static final Logger log = LoggerFactory.getLogger(Trader.class);
	public final TradingManager tradingManager;
	private AccountManager accountManager;

	private StrategyMonitor[] monitors;

	private final TradeSet trades = new TradeSet();
	final boolean allowMixedStrategies;
	final OrderListener[] notifications;
	private int pipSize;
	private final List<Trade> stoppedOut = new ArrayList<>();
	private final AtomicLong id;
	boolean liquidating = false;
	public final Context context;
	private final SignalRepository signalRepository;


	Trader(TradingManager tradingManager, StrategyMonitor[] strategyMonitors) {
		this.id = tradingManager.getAccount().getTradeIdGenerator();
		this.tradingManager = tradingManager;

		this.context = tradingManager.context;
		this.context.trader = this;

		this.monitors = strategyMonitors;
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
		this.accountManager = tradingManager.getAccount();
		this.signalRepository = accountManager.signalRepository.get();
	}

	/**
	 * Returns the parameters used by the {@link StrategyMonitor} instances in this {@code Trader} instance. Used
	 * mainly to report which parameters are being used in a parameter optimization process.
	 *
	 * @return the parameters tested in the {@link StrategyMonitor} instances of this {@code Trader}.
	 */
	public Parameters parameters() {
		return context.parameters;
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
	 * {@link #monitors()} will have their {@link StrategyMonitor#handleStop(Trade)} method called
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
		context.latestCandle(candle);
		context.signal = signal;
		context.strategy = strategy;
		context.strategyMonitor = null;
		context.exitReason = null;

		removeFinalizedTrades();

		boolean hasPosition = tradingManager.hasPosition(candle, false, true, true);

		if (hasPosition) {
			stoppedOut.clear();
			for (int i = trades.i - 1; i >= 0; i--) {
				Trade trade = trades.elements[i];
				if (trade.tick(candle, strategy) != null) {
					stoppedOut.add(trade);
				}
			}
		}
		if (!stoppedOut.isEmpty()) {
			for (Trade stoppedTrade : stoppedOut) {
				context.trade = stoppedTrade;
				exit();
			}
		}

		if (signal == BUY) {
			processBuy();
		} else if (signal == SELL) {
			processSell();
		}
		stoppedOut.clear();
	}

	private void processBuy() {
		boolean isShort = isShort(context.strategy);
		boolean isLong = isLong(context.strategy);

		boolean bought = false;

		for (int i = trades.i - 1; i >= 0; i--) {
			Trade trade = trades.elements[i];
			if (stoppedOut.contains(trade)) {
				continue;
			}
			if (isShort && trade.isShort() && trade.exitOnOppositeSignal()) {
				context.exitReason = "Buy signal";
				context.trade = trade;
				bought |= exit();
			} else if (isLong && trade.isLong()) {
				context.trade = trade;
				bought |= buy(LONG); //increment position on existing trade
			}
		}
		if (!bought) {
			if (isLong) {
				buy(LONG); //opens new trade. Can only go long here.
			}
			if (isShort) {
				boolean hasShortPosition = tradingManager.hasPosition(context.latestCandle, false, false, true);
				if (hasShortPosition) {
					// Buys without having a short trade open. Might happen after starting up
					// with short order in the account. Will generate a warning in the log.
					buy(SHORT);
				}
			}
		}
	}

	private void processSell() {
		boolean isShort = isShort(context.strategy);
		boolean isLong = isLong(context.strategy);

		boolean sold = false;
		boolean noLongs = true;
		boolean noShorts = true;

		for (int i = trades.i - 1; i >= 0; i--) {
			Trade trade = trades.elements[i];
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
				context.trade = trade;
				sold |= sellShort();
			} else if (isLong && trade.isLong() && trade.exitOnOppositeSignal()) {
				noLongs = false;
				context.trade = trade;
				context.exitReason = "Sell signal";
				sold |= exit();
			}
		}
		if (!sold) {
			if (isShort && noShorts) {
				sellShort();
			}
			if (isLong && noLongs) {
				boolean hasLongPosition = tradingManager.hasPosition(context.latestCandle, false, true, false);
				if (hasLongPosition) {
					if (accountManager.hasOtherOpenTrades(tradingManager)) {
						return; //another trading group is managing trades for this symbol.
					}
					// Sell without having a trade open. Might happen after starting up
					// with assets in the account. Will generate a warning in the log.
					context.trade = Trade.createPlaceholder(-1, this, LONG);
					context.exitReason = "Sell signal";
					sellAssets();
				}
			}
		}
	}

	private boolean exit() {
		Trade trade = context.trade;
		if (trade.canExit(context.strategy)) {
			boolean notEmptyBeforeCancellations = !trade.isEmpty();
			for (int i = trade.position.i - 1; i >= 0; i--) {
				Order order = getOrder(trade.position, i);
				if (!order.isFinalized()) {
					tradingManager.cancelOrder(order);
				}
			}
			if (notEmptyBeforeCancellations && trade.isEmpty()) {
				return false;
			}

			//tradingManager.getAccount().executeUpdateBalances();

			if (!tradingManager.hasPosition(context.latestCandle, false, trade.isLong(), trade.isShort())) {
				if (tradingManager.hasPosition(context.latestCandle, true, trade.isLong(), trade.isShort())) {
					if (log.isTraceEnabled()) {
						log.trace("Ignoring exit signal of {}: no free assets ({}) as {} are already locked in order", symbol(), tradingManager.getAssets(), tradingManager.getTotalAssets());
					}
				} else if (!trade.isEmpty()) {
					if (log.isTraceEnabled()) {
						log.trace("Ignoring exit signal of {}: no assets ({}). Sold manually? Closing trade", symbol(), tradingManager.getAssets());
					}
					//no assets available to sell, cancel any pending orders.
					trade.finalizeTrade();
				} // else trade object is empty and reused from previous attempt to create order which was cancelled.
				return false;
			}

			if (trade.isLong()) {
				return sellAssets();
			} else if (trade.isShort() && trade.exitOrders.isEmpty()) {
				return closeShort();
			}
		}
		return false;

	}

	boolean isLong(Strategy strategy) {
		return strategy == null || (strategy.tradeSide() == LONG || strategy.tradeSide() == null);
	}

	boolean isShort(Strategy strategy) {
		return tradingManager.canShortSell() && (strategy == null || (strategy.tradeSide() == SHORT || strategy.tradeSide() == null));
	}

	private double prepareTrade(Trade.Side side) {
		Strategy strategy = context.strategy;
		boolean isLong = side == LONG && isLong(strategy);
		boolean isShort = side == SHORT && isShort(strategy);

		for (int i = 0; i < monitors.length; i++) {
			context.strategyMonitor = monitors[i];
			if ((isLong && monitors[i].discardBuy(strategy)) || (isShort && monitors[i].discardShortSell(strategy))) {
				return -1.0;
			}
		}
		for (int i = trades.i - 1; i >= 0; i--) {
			Trade trade = trades.elements[i];
			if (trade.tryingToExit() && ((trade.isLong() && isLong) || (trade.isShort() && isShort))) {
				if (log.isTraceEnabled()) {
					if (isLong) {
						log.trace("Discarding buy of {} @ {}: attempting to sell current {} units", tradingManager.getSymbol(), context.latestCandle.close, trade.quantityInPosition());
					} else {
						log.trace("Discarding short sell of {} @ {}: attempting to buy more", tradingManager.getSymbol(), context.latestCandle.close);
					}
				}
				return -1.0;
			}
		}

		if (strategy != null && strategy.exitOnOppositeSignal()) {
			if ((isLong && tradingManager.waitingForBuyOrderToFill(side)) || (isShort && tradingManager.waitingForSellOrderToFill(side))) {
				tradingManager.cancelStaleOrdersFor(side, this);
				if ((isLong && tradingManager.waitingForBuyOrderToFill(side)) || (isShort && tradingManager.waitingForSellOrderToFill(side))) {
					if (log.isTraceEnabled()) {
						if (isLong) {
							log.trace("Discarding buy of {} @ {}: got buy order waiting to be filled", tradingManager.getSymbol(), context.latestCandle.close);
						} else {
							log.trace("Discarding short sell of {} @ {}: got sell order waiting to be filled", tradingManager.getSymbol(), context.latestCandle.close);
						}
					}
					return -1.0;
				}
			}
		}
		if ((isLong && tradingManager.isBuyLocked()) || (isShort && tradingManager.isShortSellLocked())) {
			if (log.isTraceEnabled()) {
				if (isLong) {
					log.trace("Discarding buy of {} @ {}: purchase order already being processed", tradingManager.getSymbol(), context.latestCandle.close);
				} else {
					log.trace("Discarding short sell of {} @ {}: sell order already being processed", tradingManager.getSymbol(), context.latestCandle.close);
				}
			}
			return -1.0;
		}
		double amountToSpend = tradingManager.allocateFunds(side);
		final double minimum = priceDetails().getMinimumOrderAmount(context.latestCandle.close);

		if (amountToSpend <= minimum) {
			tradingManager.cancelStaleOrdersFor(side, this);
			amountToSpend = tradingManager.allocateFunds(side);
			if (amountToSpend <= minimum) {
				if (tradingManager.exitExistingPositions(tradingManager.assetSymbol, context.latestCandle)) {
					tradingManager.updateBalances();
					return amountToSpend;
				} else if (!tradingManager.getAccount().isSimulated()) {
					tradingManager.updateBalances();
					amountToSpend = tradingManager.allocateFunds(side);
					if (amountToSpend <= minimum) {
						if (log.isTraceEnabled()) {
							if (isLong) {
								log.trace("Discarding buy of {} @ {}: not enough funds to allocate (${})", symbol(), context.latestCandle.close, tradingManager.getCash());
							} else {
								log.trace("Discarding short selling of {} @ {}: not enough funds to allocate (${})", symbol(), context.latestCandle.close, tradingManager.getCash());
							}
						}
						return amountToSpend;
					}
				}
			}
		}
		return amountToSpend;
	}

	private boolean sellShort() {
		double amountToSpend = prepareTrade(SHORT);
		if (amountToSpend > 0) {
			Order order = tradingManager.sell(amountToSpend / context.latestCandle.close, SHORT);
			if (order != null) {
				processOrder(order);
				return true;
			}
			if (log.isTraceEnabled()) {
				log.trace("Could not short {} @ {}", tradingManager.getSymbol(), context.latestCandle.close);
			}
		}
		return false;
	}


	private boolean buy(Trade.Side tradeSide) {
		Order order = null;
		if (tradeSide == LONG) {
			double amountToSpend = prepareTrade(tradeSide);
			if (amountToSpend <= 0) {
				return false;
			}
			order = tradingManager.buy(LONG, amountToSpend / context.latestCandle.close);
		} else if (tradeSide == SHORT) {
			double shortedQuantity = accountManager.getBalance(referenceCurrencySymbol(), Balance::getShorted);
			order = tradingManager.buy(SHORT, shortedQuantity);
		}
		if (order != null) {
			processOrder(order);
			return true;
		}
		if (log.isTraceEnabled()) {
			log.trace("Could not buy {} @ {}", tradingManager.getSymbol(), context.latestCandle.close);
		}

		return false;
	}

	public double allocateFunds(Trade.Side tradeSide) {
		final double minimum = priceDetails().getMinimumOrderAmount(context.latestCandle.close);
		double funds = tradingManager.allocateFunds(tradeSide);
		if (funds < minimum) {
			return 0.0;
		}
		return funds;
	}

	private boolean sellAssets() {
		double quantity;
		Trade trade = context.trade;
		if (trade != null) {
			quantity = trade.quantityInPosition();
			if (quantity > tradingManager.getAssets() || quantity == 0.0) {
				quantity = tradingManager.getAssets();
			}
		} else {
			quantity = tradingManager.getAssets();
		}

		Order order = tradingManager.sell(quantity, trade.getSide());
		if (order != null) {
			processOrder(order);
			return true;
		}
		return false;
	}

	private boolean closeShort() {
		double reserveFunds = accountManager.getBalance(fundSymbol(), b -> b.getMarginReserve(assetSymbol()));
		double shortToCover = accountManager.getBalance(assetSymbol(), Balance::getShorted);
		if (shortToCover > 0) {
			Trade trade = context.trade;
			if (shortToCover * trade.lastClosingPrice() > reserveFunds) {
				log.warn("Not enough funds in margin reserve to cover short of {} {} @ {} {} per unit. Reserve: {}, required {} {}", assetSymbol(), shortToCover, trade.lastClosingPrice(), fundSymbol(), reserveFunds, shortToCover * trade.lastClosingPrice(), fundSymbol());
			}

			Order order = tradingManager.buy(SHORT, shortToCover);
			if (order != null) {
				processOrder(order);
				return true;
			}
		}
		return false;
	}


	private void cancelOpenBuyOrders(Strategy strategy) {
		for (int i = trades.i - 1; i >= 0; i--) {
			cancelOpenBuyOrders(trades.elements[i], strategy);
		}
	}

	private void cancelOpenBuyOrders(Trade trade, Strategy strategy) {
		if (trade.canExit(strategy)) {
			for (int i = trade.position.i - 1; i >= 0; i--) {
				Order order = getOrder(trade.position, i);
				if (!order.isFinalized()) {
					tradingManager.cancelOrder(order);
				}
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
	public final Candle latestCandle() {
		return context.latestCandle;
	}

	public void notifySimulationEnd() {
		tradingManager.notifySimulationEnd();
	}

	public void liquidateOpenPositions() {
		liquidating = true;
		try {
			for (int i = trades.i - 1; i >= 0; i--) {
				Trade t = trades.elements[i];
				if (!t.isFinalized()) {
					if (t.isLong()) {
						context.exitReason = "Liquidate position";
						context.trade = t;
						if (!sellAssets()) {
							accountManager.consumeBalance(assetSymbol(), b -> {
								if (b.getFree() + b.getLocked() > 0) {
									log.warn("Could not liquidate open trade: " + t);
								}
							});
						} else {
							t.liquidate();
						}
					} else if (t.isShort()) {
						context.exitReason = "Liquidate position";
						context.trade = t;
						if (!closeShort()) {
							accountManager.consumeBalance(assetSymbol(), b -> {
								if (b.getShorted() > 0) {
									log.warn("Could not liquidate open trade: " + t);
								}
							});
						} else {
							t.liquidate();
						}
					}
				}
			}
		} finally {
			liquidating = false;
		}
	}

	public Order submitOrder(Order.Type type, Order.Side side, Trade.Side tradeSide, double quantity) {
		return tradingManager.submitOrder(this, quantity, side, tradeSide, type);
	}

	void processOrder(Order order) {
		if (order == null || (order.isCancelled() && order.getFillPct() == 0.0)) {
			return;
		}
		try {
			try {
				context.trade = null;
				if (order.isBuy()) {
					context.trade = processBuyOrder(order);
				} else if (order.isSell()) {
					context.trade = processSellOrder(order);
				}
				if (signalRepository != null) {
					signalRepository.add(symbol(), order.isBuy() ? BUY : SELL, latestCandle());
				}
			} finally {
				tradingManager.updateBalances();
			}
		} catch (Exception e) {
			log.error("Error processing " + order.getSide() + " order: " + order, e);
		} finally {
			tradingManager.notifyOrderSubmitted(order, context.trade);
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

	private Trade processBuyOrder(Order order) {
		Trade trade = null;
		for (int i = trades.i - 1; i >= 0; i--) {
			Trade t = trades.elements[i];
			if (!t.isFinalized() && t.getSide() == order.getTradeSide()) {
				if (order.isLong()) {
					if (t.increasePosition(order)) {
						trade = t;
						break;
					}
				} else if (order.isShort()) {
					t.decreasePosition(order, context.exitReason);
					trade = t;
					break;
				}

			}
		}


		if (trade == null) {
			if (order.isLong()) {
				trade = new Trade(id.incrementAndGet(), order, this, context.strategy);
				trades.addOrReplace(trade);
			} else if (order.isShort()) {
				trade = Trade.createPlaceholder(-1, this, order.getTradeSide());
				trade.decreasePosition(order, "Exit short position");
				log.warn("Received a short-covering buy order without an open trade: " + order);
			}
		}
		attachOrdersToTrade(trade, order);
		return trade;
	}


	private Trade processSellOrder(Order order) {
		Trade trade = context.trade;
		if (trade == null) {
			for (int i = trades.i - 1; i >= 0; i--) {
				Trade t = trades.elements[i];
				if (!t.isFinalized() && t.canExit(context.strategy)) {
					trade = t;
					break;
				}
			}
		}

		if (trade != null) {
			if (trade.isShort()) {
				if (!trade.increasePosition(order)) {
					trade = new Trade(id.incrementAndGet(), order, this, context.strategy);
					trades.addOrReplace(trade);
				}
			} else {
				trade.decreasePosition(order, context.exitReason);
			}
		} else {
			if (order.isShort()) {
				trade = new Trade(id.incrementAndGet(), order, this, context.strategy);
				trades.addOrReplace(trade);
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
	 *
	 * @return a flag indicating whether an order for a direct switch was opened. If {@code true}, the trader won't try to create a BUY order for the given
	 * exitSymbol. When {@code false} the trader
	 * will try to buy into the exitSymbol regardless of whether the current position was closed or not.
	 */
	boolean switchTo(String exitSymbol, Candle candle, String candleTicker) {
		try {
			if (exitSymbol.equals(tradingManager.fundSymbol) || exitSymbol.equals(tradingManager.assetSymbol)) {
				return false;
			}
			for (int i = trades.i - 1; i >= 0; i--) {
				Trade trade = trades.elements[i];
				if (!trade.isFinalized() && trade.allowTradeSwitch(exitSymbol, candle, candleTicker)) {
					context.trade = trade;
					context.exitReason = "exit to buy " + exitSymbol;
					if (exit()) {
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
		if (context.latestCandle == null) {
			return 0.0;
		}
		return context.latestCandle.close;
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

	public double freeBalance() {
		return accountManager.getBalance(referenceCurrencySymbol(), Balance::getFree);
	}

	public double shortedQuantity() {
		return accountManager.getBalance(assetSymbol(), Balance::getShorted);
	}

	public Balance balanceOf(String symbol) {
		return accountManager.queryBalance(symbol, Balance::clone);
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

	boolean orderFinalized(Order order) {
		Trade trade = order.getTrade();
		if (trade != null) {
			trade.orderFinalized(order);
			return true;
		}
		return false;
	}

	void removeFinalizedTrades() {
		if (!trades.isEmpty()) {
			for (int i = trades.i - 1; i >= 0; i--) {
				Trade trade = trades.elements[i];
				if (trade.isFinalized()) {
					trades.remove(trade);
				}
			}
		}
	}

	public Strategy strategyOf(Order order) {
		if (!allowMixedStrategies) {
			return order.getTrade().openingStrategy();
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
		return trades.asSet();
	}

	public boolean hasOpenTrades() {
		return !trades.isEmpty();
	}

	Order getOrder(OrderSet set, int i) {
		return set.elements[i] = tradingManager.orderTracker.getOrder(set.elements[i]);
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
