package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.simulation.orderfill.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

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

	public enum Side {
		LONG,
		SHORT
	}

	private String exitReason;
	private double averagePrice = 0.0;
	private final Map<String, Order> position = new ConcurrentHashMap<>();
	private final Map<String, Order> exitOrders = new ConcurrentHashMap<>();
	private final Map<String, Order> pastOrders = new ConcurrentHashMap<>();

	//these two are used internally only to calculate
	// average prices with fees taken into account.
	private double totalSpent;
	private double totalUnits;

	private int ticks;
	private double max;
	private double min;
	private double minChange;
	private double maxChange;
	private double change;
	private Candle firstCandle;
	private Strategy openingStrategy;
	private boolean stopped = false;

	private double finalizedQuantity = -1.0;

	private double actualProfitLoss;
	private double actualProfitLossPct;
	private final StrategyMonitor[] monitors;

	private final Trader trader;
	private Side side;
	final boolean isPlaceholder;
	private final long id;
	private boolean finalized;

	Trade(long id, Order openingOrder, Trader trader, Strategy openingStrategy) {
		this(id, trader, openingOrder.isSell() ? Side.SHORT : Side.LONG, openingStrategy, trader.monitors(), false);
		increasePosition(openingOrder);
	}

	private Trade(long id, Trader trader, Trade.Side side, Strategy openingStrategy, StrategyMonitor[] monitors, boolean isPlaceholder) {
		this.id = id;
		this.trader = trader;
		this.monitors = monitors;
		this.openingStrategy = openingStrategy;
		this.side = side;
		this.isPlaceholder = isPlaceholder;
		initTrade();
	}

	private void initTrade() {
		this.firstCandle = trader.latestCandle();
		this.max = this.min = firstCandle.close;
		finalized = false;
	}

	static Trade createPlaceholder(long id, Trader trader, Trade.Side side) {
		return new Trade(id, trader, side, null, new StrategyMonitor[0], true);
	}

	public boolean isShort() {
		return side == Side.SHORT;
	}

	public boolean isLong() {
		return side == Side.LONG;
	}

	public Trade.Side getSide() {
		return side;
	}

	private void updateMinAndMaxPrices(Candle candle) {
		if (max < candle.close) {
			max = candle.close;
		}
		if (min > candle.close) {
			min = candle.close;
		}
	}

	public String tick(Candle candle, Signal signal, Strategy strategy) {
		if (isPlaceholder) {
			return null;
		}
		if (strategy != null && strategy.tradeSide() != null && strategy.tradeSide() != this.side) {
			return null;
		}

		if (this.openingStrategy == strategy || this.openingStrategy == null) {
			averagePrice = 0.0;

			double nextChange = priceChangePct(candle.close);
			ticks++;
			updateMinAndMaxPrices(candle);
			change = nextChange;

			double prevMax = maxChange;
			double prevMin = minChange;

			maxChange = isLong() ? priceChangePct(max) : priceChangePct(min);
			minChange = isLong() ? priceChangePct(min) : priceChangePct(max);

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
		if (!stopped) {
			for (int i = 0; i < monitors.length; i++) {
				String exit = monitors[i].handleStop(this, signal, strategy);
				if (exit != null) {
					stopped = true;
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
		if (traded()) {
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
		if (traded()) {
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
	public String formattedPriceChangePct(double paid) {
		if (isLong()) {
			return formattedLongPriceChangePct(paid);
		} else {
			return formattedShortPriceChangePct(paid);
		}
	}

	private String formattedShortPriceChangePct(double totalSpent) {
		double pct = shortPriceChangePct(totalSpent);
		return formattedPct(pct);
	}

	private double shortPriceChangePct(double totalSpent) {
		return negativePriceChangePct(averagePrice(), totalSpent);
	}

	private double priceChangePct(double spent) {
		if (isLong()) {
			return longPriceChangePct(spent);
		} else {
			return shortPriceChangePct(spent);
		}
	}

	public static double positivePriceChangePct(double spent, double currentPrice) {
		return ((currentPrice - spent) / spent) * 100.0;
	}

	public static double negativePriceChangePct(double spent, double currentPrice) {
		return ((spent - currentPrice) / spent) * 100.0;
	}

	private double longPriceChangePct(double price) {
		return positivePriceChangePct(averagePrice(), price);
	}

	private String formattedLongPriceChangePct(double spent) {
		double pct = longPriceChangePct(spent);
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
	public int ticks() {
		if (traded()) {
			return ticks;
		}
		return 0;
	}

	private void updateAveragePrice(Collection<Order> orders) {
		if (isPlaceholder) {
			return;
		}
		change = maxChange = minChange = 0.0;

		//calculate average price
		totalSpent = 0.0;
		totalUnits = 0.0;
		for (Order order : orders) {
			double fees = order.getFeesPaid();
			totalSpent += order.getTotalTraded() + (order.isBuy() ? fees : -fees);
			totalUnits += order.getExecutedQuantity();
		}
		if (totalUnits == 0.0) {
			averagePrice = 0.0;
		} else {
			averagePrice = totalSpent / totalUnits;
			change = isLong() ? positivePriceChangePct(averagePrice, trader.lastClosingPrice()) : -positivePriceChangePct(averagePrice, trader.lastClosingPrice());
			maxChange = isLong() ? positivePriceChangePct(averagePrice, maxPrice()) : -positivePriceChangePct(averagePrice, minPrice());
			minChange = isLong() ? positivePriceChangePct(averagePrice, minPrice()) : priceChangePct(maxPrice());
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

	public long tradeDuration() {
		if (traded()) {
			return trader.latestCandle().closeTime - firstCandle.closeTime;
		}
		return 0L;

	}

	private boolean traded() {
		return !position.isEmpty() || finalizedQuantity > 0.0;
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

	public double actualProfitLossInReferenceCurrency() {
		return actualProfitLossIn(trader.referenceCurrencySymbol());
	}

	public double actualProfitLossIn(String currency) {
		if (currency.equals(trader.fundSymbol())) {
			return actualProfitLoss;
		}
		if (trader.assetSymbol().equals(currency)) {
			return actualProfitLoss / lastClosingPrice();
		}
//		double price = trader.tradingManager.getLatestPrice(currency, trader.fundSymbol());
//		if (price > 0) {
//			return actualProfitLoss * price;
//		} else {
//			price = trader.tradingManager.getLatestPrice(trader.fundSymbol(), currency);
//			if (price > 0) {
//				return actualProfitLoss / price;
//			}
//		}

		throw new IllegalStateException("Unable to convert profit/loss of " + actualProfitLoss + " " + trader.fundSymbol() + " to " + currency);
	}

	public double actualProfitLossPct() {
		return actualProfitLossPct;
	}

	public double breakEvenChange() {
		return trader.breakEvenChange(averagePrice());
	}

	public boolean stopped() {
		return stopped;
	}

	public Collection<Order> position() {
		return Collections.unmodifiableCollection(position.values());
	}

	void orderFinalized(Order order) {
		if (isPlaceholder) {
			return;
		}
		if (order.getExecutedQuantity() == 0) { // nothing filled, cancelled
			position.remove(order.getOrderId());
			return;
		}

		if (position.containsKey(order.getOrderId())) {
			if (order.isBuy()) {
				updateAveragePrice(position.values());
			}
		} else if (exitOrders.containsKey(order.getOrderId())) {
			if (isFinalized()) {
				updateAveragePrice(exitOrders.values());
				double totalSold = this.totalSpent;
				double soldUnits = this.totalUnits;
				double exitPrice = averagePrice;

				updateAveragePrice(position.values());

				final double cost = (totalSpent * (soldUnits / this.totalUnits));
				actualProfitLoss = totalSold - cost;
				if (Double.isNaN(actualProfitLoss)) {
					throw new IllegalStateException("Profit/loss amount can't be determined");
				}

				actualProfitLossPct = positivePriceChangePct(averagePrice, exitPrice);
				if (Double.isNaN(actualProfitLossPct)) {
					throw new IllegalStateException("Profit/loss % can't be determined");
				}
				pastOrders.putAll(position);
				position.clear();
				pastOrders.putAll(exitOrders);
				exitOrders.clear();
			} else {
				double totalSold = order.getTotalTraded();
				double sellPrice = order.getAveragePrice();

				updateAveragePrice(position.values());

				actualProfitLossPct = positivePriceChangePct(averagePrice, sellPrice);
				actualProfitLoss = totalSold - (order.getExecutedQuantity() * averagePrice);
			}

			if (isShort()) {
				actualProfitLoss = -actualProfitLoss;
				actualProfitLossPct = -actualProfitLossPct;
			}
		}
	}

	private double removeCancelledAndSumQuantities(Map<String, Order> orders) {
		double total = 0;

		if (!orders.isEmpty()) {
			var it = orders.entrySet().iterator();
			while (it.hasNext()) {
				Order order = it.next().getValue();
				if (order.isCancelled() && order.getExecutedQuantity() == 0) {
					it.remove();
				} else {
					total = total + order.getExecutedQuantity();
				}
			}
		}

		return total;
	}

	public boolean isFinalized() {
		if (finalized) {
			return true;
		}
		return finalized = checkIfFinalized();
	}

	private boolean checkIfFinalized() {
		if (isPlaceholder || position.isEmpty()) {
			return true;
		}

		if (exitOrders.isEmpty()) {
			return false;
		}

		synchronized (this) {
			double qtyInPosition = removeCancelledAndSumQuantities(position);
			if (qtyInPosition == 0.0) {
				return false;
			}
			double qtyInExit = removeCancelledAndSumQuantities(exitOrders);

			double exitPct = qtyInExit * 100.0 / qtyInPosition;

			if ((qtyInPosition - qtyInExit) * lastClosingPrice() < trader.tradingManager.minimumInvestmentAmountPerTrade() || exitPct > 98.0) {
				double fractionRemaining = qtyInPosition - qtyInExit;
				finalizedQuantity = qtyInPosition - fractionRemaining;
				return true;
			}

			return false;
		}
	}

	public Strategy openingStrategy() {
		return openingStrategy;
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
		return formattedPct(estimateProfitLossPercentage(order));
	}

	public double estimateProfitLossPercentage(Order order) {
		double change = priceChangePct();
		if (!order.isActive() && order.getTriggerPrice() != 0.0) {
			double price = order.getTriggerPrice();
			change = isLong() ? positivePriceChangePct(price, trader.lastClosingPrice()) : -positivePriceChangePct(price, trader.lastClosingPrice());
		}
		return change - 100.0 * ((trader.tradingFees().feesOnOrder(order)) / order.getTotalOrderAmount());
	}

	public boolean tryingToExit() {
		if (isPlaceholder) {
			return true;
		}
		if (!exitOrders.isEmpty()) {
			for (Order order : exitOrders.values()) {
				if (!order.isFinalized()) {
					return true;
				}
			}
		}
		return false;
	}

	boolean canExit(Strategy strategy) {
		if (isPlaceholder) {
			return false;
		}
		if ((this.side == Side.SHORT && !trader.isShort(strategy)) || (this.side == Side.LONG && !trader.isLong(strategy))) {
			return false;
		}

		if (this.openingStrategy == null || strategy == null || trader.allowMixedStrategies || this.openingStrategy == strategy) {
			for (int i = 0; i < monitors.length; i++) {
				if (!monitors[i].allowExit(this)) {
					return false;
				}
			}
		}
		return true;
	}

	public synchronized boolean increasePosition(Order order) {
		if (finalized) {
			if (position.isEmpty()) {
				initTrade();
			} else {
				throw new IllegalStateException("Trying to increase position of finalized trade");
			}
		}
		removeCancelledAndSumQuantities(exitOrders);

		if (exitOrders.isEmpty()) {
			this.position.put(order.getOrderId(), order);
			List<Order> attachments = order.getAttachments();
			if (attachments != null) {
				for (Order attachment : order.getAttachments()) {
					if (attachment.getSide() != order.getSide()) {
						exitOrders.put(attachment.getOrderId(), attachment);
					} else {
						position.put(attachment.getOrderId(), attachment);
					}
				}
			}
			notifyOrderSubmission(order);
			return true;
		}
		return false;
	}

	public synchronized void decreasePosition(Order order, String exitReason) {
		if (!isPlaceholder && finalized) {
			throw new IllegalStateException("Trying to decrease position of finalized trade");
		}
		if (this.exitReason == null) {
			this.exitReason = exitReason;
		}
		exitOrders.put(order.getOrderId(), order);
		notifyOrderSubmission(order);
	}

	private void notifyOrderSubmission(Order order) {
		updateMinAndMaxPrices(latestCandle());
		for (int i = 0; i < monitors.length; i++) {
			if (order.isSell()) {
				monitors[i].sold(this, order);
			} else if (order.isBuy()) {
				monitors[i].bought(this, order);
			}
		}
	}

	public boolean hasOrder(Order order) {
		if ((order.isBuy() && order.isLong()) || (order.isSell() && order.isShort())) {
			if (position.containsKey(order.getOrderId()) || order.getParentOrderId() != null && exitOrders.containsKey(order.getParentOrderId())) {
				return true;
			}
		} else if ((order.isSell() && isLong()) || (order.isShort() && order.isBuy()) || isPlaceholder) {
			if (exitOrders.containsKey(order.getOrderId()) || order.getParentOrderId() != null && position.containsKey(order.getParentOrderId())) {
				return true;
			}
		}
		return pastOrders.get(order.getOrderId()) != null || order.getParentOrderId() != null && pastOrders.containsKey(order.getParentOrderId());
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
		return Long.compare(this.id, o.id);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Trade)) {
			return false;
		}
		return id == ((Trade) obj).id;
	}

	public String symbol() {
		return trader.symbol();
	}

	public double quantity() {
		if (finalizedQuantity < 0) {
			return removeCancelledAndSumQuantities(position);
		} else {
			return finalizedQuantity;
		}
	}

	public String formattedMinPriceAndPercentage() {
		SymbolPriceDetails f = trader.priceDetails();
		return '$' + f.priceToString(minPrice()) + " (" + (isLong() ? formattedMinChangePct() : formattedMaxChangePct()) + ')';
	}

	public String formattedMaxPriceAndPercentage() {
		SymbolPriceDetails f = trader.priceDetails();
		return '$' + f.priceToString(maxPrice()) + " (" + (isLong() ? formattedMaxChangePct() : formattedMinChangePct()) + ')';
	}

	public String toString() {
		return position.toString() + exitOrders.toString();
	}

	public double quantityInPosition() {
		double pos = removeCancelledAndSumQuantities(position);
		double exit = removeCancelledAndSumQuantities(exitOrders);
		if (pos - exit < 0) {
			throw new IllegalStateException("Illegal quantity of " + symbol() + " held in " + getSide() + " trade. Position: " + pos + ", Exit: " + exit);
		}
		return pos - exit;
	}

	public void liquidate() {
		ImmediateFillEmulator immediateFill = new ImmediateFillEmulator();
		for (Order order : exitOrders()) {
			if (!order.isFinalized()) {
				immediateFill.fillOrder((DefaultOrder) order, latestCandle());
				trader.tradingManager.notifyOrderFinalized(order, this);
			}
		}
	}

	public long id() {
		return id;
	}
}
