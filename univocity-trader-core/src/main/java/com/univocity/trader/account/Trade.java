package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.account.Balance.*;

/**
 * A {@code Trade} holds one or more {@link Order} placed in the {@link Exchange} through
 * a {@link Trader}. Once a position is opened, this {@code Trade} object will start capturing the following
 * information:
 * <ul>
 * <li>the number of ticks received since the first trade was created via {@link #ticks()}</li>
 * <li>the average price paid for the instrument traded by this {@code Trader} via {@link #averagePrice()}</li>
 * <li>the time elapsed since the first trade executed via {@link #tradeDuration()};</li>
 * <li>the current change % in price since this trade opened via {@link #priceChangePct()}</li>
 * <li>the maximum positive change % this trade had via {@link #maxChange()}</li>
 * <li>the maximum price reached since the trade opened via {@link #maxPrice()}</li>
 * <li>the minimum change % (negative or zero) this trade had via {@link #minChange()}</li>
 * <li>the minimum price reached since the trade opened via {@link #minPrice()}</li>
 * <li>the latest closing price of the instrument traded via {@link #lastClosingPrice()}</li>
 * <li>the minimum positive change % required to break even after fees, via {@link #breakEvenChange()}</li>
 * </ul>
 * These statistics take into account one {@link Order} or more which represent the current
 * position being held.
 */
public class Trade implements Comparable<Trade> {
	private static final Logger log = LoggerFactory.getLogger(Trade.class);

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
	private final StrategyMonitor[] monitors;

	private final Trader trader;

	Trade(Order buyOrder, Trader trader, Strategy boughtStrategy) {
		this.trader = trader;
		this.firstCandle = trader.latestCandle();
		this.boughtStrategy = boughtStrategy;
		this.monitors = trader.monitors();

		increasePosition(buyOrder);
	}

	public String tick(Candle candle, Signal signal, Strategy strategy) {
		if (this.boughtStrategy == strategy || this.boughtStrategy == null) {
			averagePrice = 0.0;

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

			double prevMin = minChange;
			minChange = priceChangePct(min);

			if (maxChange > prevMax) {
				for (int i = 0; i < monitors.length; i++) {
					monitors[i].highestProfit(this, maxChange);
				}
			}
			if (minChange < prevMin) {
				for (int i = 0; i < monitors.length; i++) {
					monitors[i].worstLoss(this, minChange);
				}
			}
		}
		if (exitReason == null) {
			for (int i = 0; i < monitors.length; i++) {
				String exit = monitors[i].handleStop(this, signal, strategy);
				if (exit != null) {
					return exitReason = exit;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a description detailing why the latest trade was closed. Typically populated from
	 * {@link StrategyMonitor#handleStop(Trade, Signal, Strategy)} when a trade is stopped without a {@code SELL}
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
		return formattedPct(priceChangePct());
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
		return averagePrice() > 0.0 ? change : 0.0;
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
			change = priceChangePct(averagePrice, trader.lastClosingPrice());
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
			return trader.latestCandle().closeTime - firstCandle.closeTime;
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

	public double actualProfitLoss() {
		return actualProfitLoss;
	}

	public double actualProfitLossPct() {
		return actualProfitLossPct;
	}

	public double breakEvenChange() {
		return trader.breakEvenChange(averagePrice());
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
			if (tradeFinalized()) {
				updateAveragePrice(exitOrders.values());
				double totalSold = this.totalSpent;
				double soldUnits = this.totalUnits;
				double sellPrice = averagePrice;

				updateAveragePrice(position.values());

				actualProfitLoss = totalSold - (totalSpent * (soldUnits / this.totalUnits));
				if (Double.isNaN(actualProfitLoss)) {
					throw new IllegalStateException("Profit/loss amount can't be determined");
				}

				actualProfitLossPct = priceChangePct(averagePrice, sellPrice);
				if (Double.isNaN(actualProfitLossPct)) {
					throw new IllegalStateException("Profit/loss % can't be determined");
				}
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

	private BigDecimal removeCancelledAndSumQuantities(Map<String, Order> orders) {
		BigDecimal total = BigDecimal.ZERO;

		if (!orders.isEmpty()) {
			var it = orders.entrySet().iterator();
			while (it.hasNext()) {
				Order order = it.next().getValue();
				if (order.isCancelled() && order.getExecutedQuantity().equals(BigDecimal.ZERO)) {
					it.remove();
				} else {
					total = total.add(order.getExecutedQuantity());
				}
			}
		}

		return total;
	}

	public boolean tradeFinalized() {
		if (position.isEmpty()) {
			if (!exitOrders.isEmpty()) {
				throw new IllegalStateException("Can't hold a position without buy order information.");
			} else {
				return true;
			}
		}
		if (exitOrders.isEmpty()) {
			return false;
		}

		BigDecimal bought = removeCancelledAndSumQuantities(position);
		BigDecimal sold = removeCancelledAndSumQuantities(exitOrders);

		BigDecimal minLeftOver = bought.multiply(new BigDecimal("0.005")); // half of a percent of total bought

		if (bought.subtract(sold).round(ROUND_MC).compareTo(minLeftOver) <= 0) {
			return true;
		}

		return false;
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
		return formattedPct(priceChangePct() - 100.0 * ((trader.tradingFees().feesOnOrder(order)) / order.getTotalOrderAmount().doubleValue()));
	}

	public boolean tryingToExit() {
		if (!exitOrders.isEmpty()) {
			for (Order order : exitOrders.values()) {
				if (!order.isFinalized()) {
					return true;
				}
			}
		}
		return false;
	}

	boolean canSell(Strategy strategy) {
		for (int i = 0; i < monitors.length; i++) {
			if (!monitors[i].allowExit(this)) {
				return false;
			}
		}

		// or received a SELL signal from a relevant strategy
		return this.boughtStrategy == null || strategy == null || trader.allowMixedStrategies || this.boughtStrategy == strategy;

	}

	public void increasePosition(Order order) {
		this.position.put(order.getOrderId(), order);
		for (int i = 0; i < monitors.length; i++) {
			monitors[i].bought(this, order);
		}
	}

	public void decreasePosition(Order order, String exitReason) {
		if (this.exitReason == null) {
			this.exitReason = exitReason;
		}
		exitOrders.put(order.getOrderId(), order);
		for (int i = 0; i < monitors.length; i++) {
			monitors[i].sold(this, order);
		}
	}

	public boolean hasOrder(Order order) {
		if (order.isBuy()) {
			for (Order o : position.values()) {
				if (o.getOrderId().equals(order.getOrderId())) {
					return true;
				}
			}
		} else if (order.isSell()) {
			for (Order o : exitOrders.values()) {
				if (o.getOrderId().equals(order.getOrderId())) {
					return true;
				}
			}
		}
		return false;
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

	public double lastClosingPrice() {
		return trader.lastClosingPrice();
	}

	boolean allowTradeSwitch(String exitSymbol, Candle candle, String candleTicker) {
		boolean canExit = monitors.length > 0;
		for (int i = 0; i < monitors.length; i++) {
			canExit &= monitors[i].allowTradeSwitch(this, exitSymbol, candle, candleTicker);
		}
		return canExit;
	}

	public Candle latestCandle() {
		return trader.latestCandle();
	}

	public Trader trader() {
		return trader;
	}

	public Collection<Order> exitOrders() {
		return Collections.unmodifiableCollection(exitOrders.values());
	}

	@Override
	public int compareTo(Trade o) {
		return Long.compare(this.firstCandle.openTime, o.firstCandle.openTime);
	}

	public String symbol() {
		return trader.symbol();
	}

	public double quantity() {
		return removeCancelledAndSumQuantities(position).doubleValue();
	}
}
