package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

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
	final StrategyMonitor[] monitors;
	private Candle latestCandle;

	private Parameters parameters;
	final boolean allowMixedStrategies;

	private String exitReason;
	private double averagePrice = 0.0;
	private final Map<String, Order> position = new ConcurrentHashMap<>();
	private final Map<String, Order> exitOrders = new ConcurrentHashMap<>();

	//these two are used internally only to calculate
	// average prices with fees taken into account.
	private double totalSpent;
	private double totalUnits;

	private int ticks;
	private double max = Double.MIN_VALUE;
	private double min = Double.MAX_VALUE;
	private double minChange;
	private double maxChange;
	private double change;
	private Candle firstCandle;
	private Strategy boughtStrategy;

	private double actualProfitLoss;
	private double actualProfitLossPct;

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
		NewInstances<StrategyMonitor> monitorProvider = tradingManager.getAccount().configuration().monitors();
		this.monitors = monitorProvider == null ? new StrategyMonitor[0] : getInstances(tradingManager.getSymbol(), parameters, monitorProvider, "StrategyMonitor", false, allInstances);
		boolean allowMixedStrategies = true;
		for (int i = 0; i < this.monitors.length; i++) {
			this.monitors[i].setTrader(this);
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
	 * Returns all {@link StrategyMonitor} instances built in the constructor of this class, which will be used by
	 * an {@link Engine} that processes candles for the symbol traded by this {@code Trader}
	 *
	 * @return all strategy monitors used by this {@code Trader}.
	 */
	public StrategyMonitor[] monitors() {
		return monitors;
	}

	/**
	 * Takes a trading decision based on the signal generated from a strategy. Once an {@link Order} is placed in the
	 * {@link Exchange}, this {@code Trader} object will start capturing the following
	 * information:
	 * <ul>
	 * <li>the number of ticks received since the first trade was created via {@link #ticks()}</li>
	 * <li>the average price paid for the instrument traded by this {@code Trader} via {@link #averagePrice()}</li>
	 * <li>the time elapsed since the first trade executed via {@link #tradeDuration()};</li>
	 * <li>the current change % in price since this trade opened via {@link #change()}</li>
	 * <li>the maximum positive change % this trade had via {@link #maxChange()}</li>
	 * <li>the maximum price reached since the trade opened via {@link #maxPrice()}</li>
	 * <li>the minimum change % (negative or zero) this trade had via {@link #minChange()}</li>
	 * <li>the minimum price reached since the trade opened via {@link #minPrice()}</li>
	 * <li>the latest closing price of the instrument traded via {@link #lastClosingPrice()}</li>
	 * <li>the minimum positive change % required to break even after fees, via {@link #breakEvenChange()}</li>
	 * </ul>
	 * These statistics take into account one {@link Order} or more which represent the current
	 * position being held.
	 *
	 * The actions taken by the trader depend on the signal received:
	 * <ul>
	 * <li>signal = {@code BUY} - an order will be submitted when:
	 * <ol>
	 * <li>there are funds available to purchase that asset (via {@link TradingManager#allocateFunds()});</li>
	 * <li>none of the associated strategy monitors (from {@link #monitors()} produce
	 * {@link StrategyMonitor#discardBuy(Strategy)};</li>
	 * <li>the {@link OrderRequest} processed by the {@link OrderManager} associated with the symbol is not cancelled
	 * (i.e. {@link OrderRequest#isCancelled()})</li>
	 * </ol>
	 * </li>
	 * <li>signal = {@code SELL}: Sells all assets held for the current symbol, closing any open orders, if:
	 * <ol>
	 * <li>the account has assets available to sell (via {@link TradingManager#hasAssets(Candle, boolean)});</li>
	 * <li>none of the associated strategy monitors (from {@link #monitors()} produce {@code false} upon
	 * invoking {@link StrategyMonitor#allowExit()};</li>
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
	 * {@link #monitors()} will have their {@link StrategyMonitor#handleStop(Signal, Strategy)} method called
	 * to determine whether or not to exit the trade. If any one of these calls return
	 * an exit message, the assets will be sold, {@link Trader#stopped()} will evaluate to {@code true} and {@link Trader#exitReason()}
	 * will return the reason for exiting the trade.
	 *
	 * @param candle   the latest candle received for the symbol traded by this {@code Trader}
	 * @param signal   the signal generated by the given strategy after receiving the given candle
	 * @param strategy the strategy that originated the signal
	 *
	 * @return a signal indicating the action taken by this {@code Trader}, i.e. {@code BUY} if it bought assets,
	 * {@code SELL} if assets were sold for whatever reason, and {@code NEUTRAL} if no action
	 * taken.
	 */
	public Signal trade(Candle candle, Signal signal, Strategy strategy) {
		latestCandle = candle;
		averagePrice = 0.0;

		exitReason = signal == SELL ? "sell signal" : null;
		if (signal == SELL && !allowMixedStrategies && this.boughtStrategy != null && strategy != this.boughtStrategy) {
			log.debug("Cleared sell signal of {}: trade opened by another strategy", symbol());
			signal = NEUTRAL;
		}
		boolean hasAssets = tradingManager.hasAssets(candle, false) /*|| !position.isEmpty()*/; //TODO: TEST THIS
		if (hasAssets) {
			double nextChange = priceChangePct(candle.close);
			ticks++;
			if (max < candle.close) {
				max = candle.close;
			}
			if (min > candle.close) {
				min = candle.close;
			}
			change = nextChange;
			double prevMax = maxChange;
			maxChange = priceChangePct(max);
			if (maxChange > prevMax) {
				for (int i = 0; i < monitors.length; i++) {
					monitors[i].highestProfit(maxChange);
				}
			}
			double prevMin = minChange;
			minChange = priceChangePct(min);
			if (minChange < prevMin) {
				for (int i = 0; i < monitors.length; i++) {
					monitors[i].worstLoss(minChange);
				}
			}
			for (int i = 0; i < monitors.length; i++) {
				String exit = monitors[i].handleStop(signal, strategy);
				if (exit != null) {
					exitReason = exit;
					signal = SELL;
					break;
				}
			}
		} else {
			change = 0.0;
		}
		Signal out = NEUTRAL;
		if (signal == BUY) {
			Strategy currentBoughtStrategy = boughtStrategy;
			try {
				boughtStrategy = strategy;
				if (buy(candle, strategy)) {
					firstCandle = candle.clone();
					for (int i = 0; i < monitors.length; i++) {
						monitors[i].bought();
					}
					currentBoughtStrategy = strategy;

					if (position.isEmpty() || !tradingManager.hasAssets(candle, true)) {
						ticks = 0;
						max = candle.close;
						min = candle.close;
					}
					out = BUY;
				}
			} finally {
				boughtStrategy = currentBoughtStrategy;
			}
		} else if (exitReason != null || signal == SELL) {
			if (sell(candle)) {
				for (int i = 0; i < monitors.length; i++) {
					monitors[i].sold();
				}
				out = SELL;
			}
		}
		return out;
	}

	private boolean buy(Candle candle, Strategy strategy) {
		for (int i = 0; i < monitors.length; i++) {
			if (monitors[i].discardBuy(strategy)) {
				return false;
			}
		}
		if (!exitOrders.isEmpty()) {
			for(Order order : exitOrders.values()){
				if(!order.isFinalized()){
					log.trace("Discarding buy of {} @ {}: attempting to sell current {} units", tradingManager.getSymbol(), candle.close, tradingManager.getAssets());
					return false;
				}
			}
		}
		if (tradingManager.waitingForBuyOrderToFill()) {
			tradingManager.cancelStaleOrdersFor(this);
			if (tradingManager.waitingForBuyOrderToFill()) {
				log.trace("Discarding buy of {} @ {}: got buy order waiting to be filled", tradingManager.getSymbol(), candle.close);
				return false;
			}
		}
		if (tradingManager.isBuyLocked()) {
			log.trace("Discarding buy of {} @ {}: purchase order already being processed", tradingManager.getSymbol(), candle.close);
			return false;
		}
		double amountToSpend = tradingManager.allocateFunds();
		final double minimum = priceDetails().getMinimumOrderAmount(candle.close);
		if (amountToSpend * candle.close <= minimum) {
			tradingManager.cancelStaleOrdersFor(this);
			amountToSpend = tradingManager.allocateFunds() / candle.close;
			if (amountToSpend * candle.close <= minimum) {
				if (tradingManager.exitExistingPositions(tradingManager.assetSymbol, candle)) {
					tradingManager.updateBalances();
					return true;
				} else {
					tradingManager.updateBalances();
					amountToSpend = tradingManager.allocateFunds() / candle.close;
					if (amountToSpend * candle.close <= minimum) {
						log.trace("Discarding buy of {} @ {}: not enough funds to allocate (${})", symbol(), candle.close, tradingManager.getCash());
						return false;
					}
				}
			}
		}
		Order order = tradingManager.buy(amountToSpend / candle.close);
		if (order != null) {
			processOrder(order);
			return true;
		}
		log.trace("Could not buy {} @ {}", tradingManager.getSymbol(), candle.close);
		return false;
	}

	private boolean canSell(Candle candle) {
		if (!tradingManager.hasAssets(candle, false)) {
			log.trace("Ignoring sell signal of {}: no assets ({})", symbol(), tradingManager.getAssets());
			return false;
		}
		for (int i = 0; i < monitors.length; i++) {
			if (!monitors[i].allowExit()) {
				return false;
			}
		}
		return true;
	}

	private boolean sell(Candle candle) {
		if (canSell(candle)) {
			for (int i = 0; i < monitors.length; i++) {
				if (monitors[i].discardSell(candle)) {
					return false;
				}
			}
			Order order = tradingManager.sell(tradingManager.getAssets());
			if (order != null) {
				processOrder(order);
				return true;
			}
		}
		return false;
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

	void processOrder(Order order) {
		if(order == null){
			return;
		}
		try {
			try {
				if (exitReason == null) {
					position.put(order.getOrderId(), order);
				} else {
					exitOrders.put(order.getOrderId(), order);
				}
			} finally {
				tradingManager.updateBalances();
			}
		} catch (Exception e) {
			log.error("Error processing " + order.getSide() + " order: " + order, e);
		} finally {
			tradingManager.notifyOrderSubmitted(order);
		}
	}

	/**
	 * Tries to exit a current trade to immediately buy into another instrument. In cases where it's supported, such as currencies and crypto, a "direct" switch will be executed to save trading fees;
	 * i.e. if there's an open position on BTCUSDT, and the exit symbol is ETH, a single SELL order of symbol BTCETH will be executed, selling BTC to open a position in ETH. If no compatible trading
	 * symbols exists, or the market operates just with stocks, the current position will be sold in order to make funds available for buying into the next instrument. Using the previous example, BTC
	 * would be sold back into USDT, and another BUY order would be made using the USDT funds to buy ETH. If any call {@link StrategyMonitor#allowTradeSwitch(String, Candle, String)} evaluates to
	 * {@code false}, the "switch" operation will be cancelled, otherwise the current open position will be closed to release funds for the trader to BUY into the exitSymbol.
	 *
	 * @param exitSymbol   the new instrument to be bought into using the funds allocated by the current open order (in {@link #symbol()}.
	 * @param candle       the latest candle of the exitSymbol that's been received from the exchange.
	 * @param candleTicker the ticker of the received candle (not the same as {@link #symbol()}).
	 *
	 * @return a flag indicating whether an order for a direct switch was opened. If {@code true}, the trader won't try to create a BUY order for the given exitSymbol. When {@code false} the trader
	 * will try to buy into the exitSymbol regardless of whether the current position was closed or not.
	 */
	boolean switchTo(String exitSymbol, Candle candle, String candleTicker) {
		try {
			if (exitSymbol.equals(tradingManager.fundSymbol) || exitSymbol.equals(tradingManager.assetSymbol)) {
				return false;
			}
			if (tradingManager.hasAssets(candle, false)) {
				boolean canExit = monitors.length > 0;
				for (int i = 0; i < monitors.length; i++) {
					canExit &= monitors[i].allowTradeSwitch(exitSymbol, candle, candleTicker);
				}
				if (canExit) { // if true, IT WILL SELL
					// boolean toExit = tradingManager.isDirectSwitchSupported(getAssetSymbol(), exitSymbol);
					// boolean fromExit = tradingManager.isDirectSwitchSupported(exitSymbol, getAssetSymbol());
					/*
					 * if (toExit || fromExit) { // Need to find out the value of the new symbol using the same "cash" unit. Example: // Sell ADA (ADAUSDT) to buy BTC (ADABTC). We need to know how much a
					 * BTC is worth in USDT to // find out how much ADA to sell on the ADABTC market. String targetTicker = toExit ? getAssetSymbol() + exitSymbol : exitSymbol + getAssetSymbol(); if
					 * (tradingManager.switchTo(targetTicker, toExit ? SELL : BUY, exitSymbol)) { boughtPrices.clear(); return true; } else if (tradingManager.sell(tradingManager.getAssets())) {
					 * boughtPrices.clear(); } } else
					 */
					Order order = tradingManager.sell(tradingManager.getAssets());
					if (order != null) {
						exitReason = "exit to buy " + exitSymbol;
						processOrder(order);
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

	public SymbolPriceDetails priceDetails() {
		return tradingManager.getPriceDetails();
	}

	public String toString() {
		return "Trader{" + symbol() + "}";
	}

	/**
	 * Returns a description detailing why the latest trade was closed. Typically populated from {@link StrategyMonitor#handleStop(Signal, Strategy)} when a trade is stopped without a {@code SELL}
	 * signal.
	 *
	 * @return the reason for exiting the latest trade.
	 */
	public String exitReason() {
		return exitReason;
	}

	/**
	 * Returns the maximum closing price reached for the current position since the first trade was made.
	 *
	 * @return the maximum closing price recorded for the traded symbol since opening latest the position.
	 */
	public double maxPrice() {
		if (!position.isEmpty()) {
			return max;
		}
		return 0.0;
	}

	/**
	 * Returns the minimum closing price reached for the current position since the first trade was made.
	 *
	 * @return the minimum closing price recorded for the traded symbol since opening latest the position.
	 */
	public double minPrice() {
		if (!position.isEmpty()) {
			return min;
		}
		return 0.0;
	}

	/**
	 * Returns the formatted most negative change percentage reached during the current (or latest) trade.
	 *
	 * @return the maximum change percentage for the trade symbol, formatted as {@code #,##0.00%}
	 */
	public String formattedMaxChangePct() {
		return formattedPct(maxChange());
	}

	/**
	 * Returns the formatted most negative change percentage reached during the current (or latest) trade.
	 *
	 * @return the maximum change percentage for the trade symbol, formatted as {@code #,##0.00%}
	 */
	public String formattedMinChangePct() {
		return formattedPct(minChange());
	}

	/**
	 * Returns the formatted current price change percentage of the current (or latest) trade.
	 *
	 * @return the current change percentage, formatted as {@code #,##0.00%}
	 */
	public String formattedPriceChangePct() {
		return formattedPct(change());
	}

	/**
	 * Returns the formatted current price change percentage of the {@link #averagePrice()} relative to a given amount.
	 *
	 * @param paid the actual amount spent on an {@link Order}
	 *
	 * @return the change percentage, formatted as {@code #,##0.00%}
	 */
	public String formattedPriceChangePct(BigDecimal paid) {
		return formattedPriceChangePct(paid.doubleValue());
	}

	private static double priceChangePct(double paid, double currentPrice) {
		double out = (currentPrice / paid) - 1.0;
		return out * 100;
	}

	private double priceChangePct(double price) {
		return priceChangePct(averagePrice(), price);
	}

	private String formattedPriceChangePct(double paid) {
		double pct = priceChangePct(paid);
		return formattedPct(pct);
	}

	String formattedPct(double percentage) {
		return Candle.CHANGE_FORMAT.get().format(percentage / 100.0);
	}

	/**
	 * Returns the current price change percentage of the current (or latest) trade
	 *
	 * @return the current change percentage, where 100% change is returned as {@code 100.0}
	 */
	public double priceChangePct() {
		return change();
	}

	/**
	 * Returns the number of ticks processed since the latest {@link Order} of the current position was submitted.
	 *
	 * @return the count of ticks registered so far for the trade.
	 */
	public int tradeLength() {
		if (!position.isEmpty()) {
			return ticks;
		}
		return 0;
	}

	private void updateAveragePrice(Collection<Order> orders) {
		change = maxChange = minChange = 0.0;

		//calculate average price
		totalSpent = 0.0;
		totalUnits = 0.0;
		for (Order order : orders) {
			double fees = order.getFeesPaid().doubleValue();
			totalSpent += order.getTotalTraded().doubleValue() + (order.isBuy() ? fees : -fees);
			totalUnits += order.getExecutedQuantity().doubleValue();
		}
		if (totalUnits == 0.0) {
			averagePrice = 0.0;
		} else {
			averagePrice = totalSpent / totalUnits;
			change = priceChangePct(averagePrice, lastClosingPrice());
			maxChange = priceChangePct(averagePrice, maxPrice());
			minChange = priceChangePct(averagePrice, minPrice());
		}
	}

	/**
	 * Returns the average price paid based on every {@link Order} opened.
	 *
	 * @return the average unit amount paid based on each individual {@link Order} in the current position.
	 */
	public double averagePrice() {
		if (averagePrice <= 0.0) {
			updateAveragePrice(position.values());
		}
		return averagePrice;
	}

	public int ticks() {
		if (!position.isEmpty()) {
			return ticks;
		}
		return 0;
	}

	public long tradeDuration() {
		if (!position.isEmpty()) {
			return latestCandle().closeTime - firstCandle.closeTime;
		}
		return 0L;

	}

	public String formattedTradeLength() {
		return TimeInterval.getFormattedDuration(tradeDuration());
	}

	public double minChange() {
		return averagePrice() > 0.0 ? minChange : 0.0;
	}

	public double maxChange() {
		return averagePrice() > 0.0 ? maxChange : 0.0;
	}

	public double change() {
		return averagePrice() > 0.0 ? change : 0.0;
	}

	public double actualProfitLoss() {
		return actualProfitLoss;
	}

	public double actualProfitLossPct() {
		return actualProfitLossPct;
	}

	public double breakEvenChange() {
		return breakEvenChange(averagePrice());
	}

	public boolean stopped() {
		return exitReason != null;
	}

	public Collection<Order> position() {
		return Collections.unmodifiableCollection(position.values());
	}

	void orderFinalized(Order order) {
		if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) == 0) { // nothing filled, cancelled
			position.remove(order.getOrderId());
			return;
		}

		if (position.containsKey(order.getOrderId())) {
			if (order.isBuy()) {
				updateAveragePrice(position.values());
			}
		} else if (exitOrders.containsKey(order.getOrderId())) {
			if (!tradingManager.hasAssets(latestCandle, true)) {
				updateAveragePrice(exitOrders.values());
				double totalSold = this.totalSpent;
				double soldUnits = this.totalUnits;
				double sellPrice = averagePrice;

				updateAveragePrice(position.values());

				actualProfitLossPct = priceChangePct(averagePrice, sellPrice);
				actualProfitLoss = totalSold - (totalSpent * (soldUnits / this.totalUnits));

				position.clear();
				exitOrders.clear();


			} else {
				double totalSold = order.getTotalTraded().doubleValue();
				double sellPrice = order.getPrice().doubleValue();

				updateAveragePrice(position.values());

				actualProfitLossPct = priceChangePct(averagePrice, sellPrice);
				actualProfitLoss = totalSold - (order.getExecutedQuantity().doubleValue() * averagePrice);
			}
		}
	}

	public Strategy boughtStrategy() {
		return boughtStrategy;
	}

	public String formattedProfitLossPct() {
		return formattedPct(actualProfitLossPct());
	}

	/**
	 * Returns the formatted current price change percentage of the current (or latest) trade.
	 *
	 * @return the current change percentage, formatted as {@code #,##0.00%}
	 */
	public String formattedEstimateProfitLossPercentage(Order order) {
		return formattedPct(change() - 100.0 * ((tradingFees().feesOnOrder(order)) / order.getTotalOrderAmount().doubleValue()));
	}
}
