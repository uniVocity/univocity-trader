package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.math.*;

import static com.univocity.trader.account.Order.Side.*;

/**
 * An {@link OrderFillEmulator} that emulates slippage.
 *
 * Considers the {@link Candle#volume} to calculate how much of the {@link Order#getQuantity()} will be filled,
 * potentially leaving the order {@code PARTIALLY_FILLED} until another suitable candle is found.
 *
 * The calculation to determine how many units will be filled uses the {@link Candle#volume} and spreads it evenly
 * through the pips between {@link Candle#low} and {@link Candle#high}.
 *
 * e.g. if {@code low = $1.25}, {@code high = $1.35} and {@code volume = 400}, 40 units will be filled
 * for each $0.01 change in price (volume per pip).
 *
 * If the {@link Order.Type} is {@code LIMIT}, and the order price is within the high/low of a future candle,
 * <ul>
 *     <li>{@code LIMIT BUY} orders will start from the {@link Candle#open} price, decrementing one pip towards the
 *         {@link Candle#low} and accumulating the volume per pip each time until {@link Order#getRemainingQuantity()}
 *         becomes zero or {@link Candle#low} is reached. {@link Order#getPrice()} will not be changed unless the
 *         {@link Candle#high} is less than {@link Order#getPrice()}.
 *     </li>
 *     <li>{@code LIMIT SELL} orders will start from the {@link Candle#open} price, incrementing one pip towards the
 *         {@link Candle#high} and accumulating the volume per pip each time until {@link Order#getRemainingQuantity()}
 * 	       becomes zero or {@link Candle#high} is reached. {@link Order#getPrice()} will not be changed unless the
 *         {@link Candle#low} is greater than {@link Order#getPrice()}.
 * 	     </li>
 * </ul>
 *
 * If the {@link Order.Type} is {@code MARKET}, the filling process will start from the {@link Candle#open} price
 * of the next candle, and calculate the average price for the volume accumulated.
 * <ul>
 *     <li>{@code MARKET BUY} orders will start from the open, incrementally adding one pip to the price while
 *     accumulating the volume per pip each time until {@link Order#getRemainingQuantity()} becomes zero,
 *     regardless of the available {@link Candle#volume}.
 *
 *     The {@link Order#getPrice()} will modified to be the average price after each pip increase multiplied by
 *     the total traded volume. This average price will then be adjusted using the formula:
 *
 *     {@code averagePrice = (averagePrice + open + ((open + close + high) / 3.0)) / 3.0}
 *
 *     Finally, if {@code averagePrice > high}, {@link Order#getPrice()} will be set to {@code high}
 *     </li>
 *
 *     <li>{@code MARKET SELL} orders will start from the open, subtracting one pip from the price while
 *     accumulating the volume per pip each time until {@link Order#getRemainingQuantity()} becomes zero,
 *     regardless of the available {@link Candle#volume}.
 *
 *     The {@link Order#getPrice()} will modified to be the average price after each pip decrease multiplied by
 *     the total traded volume. This average price will then be adjusted using the formula:
 *
 *     {@code averagePrice = (averagePrice + open + ((open + close + low) / 3.0)) / 3.0}
 *
 *     Finally, if {@code averagePrice < low}, {@link Order#getPrice()} will be set to {@code low}
 * 	   </li>
 * </ul>
 * {@code MARKET} orders will be always 100% filled based on the next candle.
 */
public class SlippageEmulator implements OrderFillEmulator {

	@Override
	public void fillOrder(DefaultOrder order, Candle candle) {
		if (order.isFinalized()) {
			return;
		}
		double quantity = order.getQuantity().doubleValue();
		double executed = order.getExecutedQuantity().doubleValue();
		if (executed >= quantity) {
			order.setStatus(Order.Status.FILLED);
			return;
		}

		double pips = (candle.high - candle.low);
		double decimals = getDecimals(pips);
		double multiplier = (Math.pow(10, decimals));

		double increment = 1.0 / multiplier;
		double totalVolume = candle.volume;
		double volumePerPip = pips == 0 ? totalVolume : totalVolume / pips;


		final boolean buying = order.getSide() == BUY;

		double price = order.getPrice().doubleValue();
		double high = candle.high;
		double low = candle.low;
		if (order.getType() == Order.Type.MARKET) { //market order, let it run until order fills
			price = candle.open;
			if (buying) {
				low = Integer.MIN_VALUE;
			} else {
				high = Integer.MAX_VALUE;
			}

		}
		quantity -= executed;

		double totalPaid = 0.0;
		double tradedVolume = 0.0;

		increment = order.getType() == Order.Type.MARKET ? -increment : increment;

		if (order.getType() == Order.Type.LIMIT || order.getType() == Order.Type.MARKET) {
			while (totalVolume > 0 && quantity > 0 && ((buying && price >= low) || (!buying && price <= high))) {
				totalVolume -= volumePerPip;
				quantity -= volumePerPip;
				if (totalVolume < 0 || quantity < 0) {
					quantity = volumePerPip - Math.abs(Math.min(totalVolume, quantity));
					executed += quantity;

					totalPaid += price * quantity;
					tradedVolume += quantity;

					break;
				} else {
					executed += volumePerPip;
					totalPaid += price * volumePerPip;
					tradedVolume += volumePerPip;
				}
				price = buying ? price - increment : price + increment;
			}

			if (order.getType() == Order.Type.MARKET) {
				double averagePrice = totalPaid / tradedVolume;
				averagePrice = (averagePrice + candle.open + ((candle.open + candle.close + (buying ? candle.high : candle.low)) / 3.0)) / 3.0;
				if (buying && averagePrice > candle.high) {
					averagePrice = candle.high;
				} else if (!buying && averagePrice < candle.low) {
					averagePrice = candle.low;
				}
				updatePrice(order, averagePrice, tradedVolume);
			} else if (order.getType() == Order.Type.LIMIT) {
				if (order.getSide() == BUY && candle.high < order.getPrice().doubleValue()) {
					updatePrice(order, candle.high, tradedVolume);
				}
				if (order.getSide() == SELL && candle.low > order.getPrice().doubleValue()) {
					updatePrice(order, candle.low, tradedVolume);
				}
			}
			order.setExecutedQuantity(BigDecimal.valueOf(executed));
			if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) > 0) {
				int scale = Math.min(order.getQuantity().scale(), order.getExecutedQuantity().scale());

				if (order.getExecutedQuantity().setScale(scale, RoundingMode.CEILING).compareTo(order.getQuantity().setScale(scale, RoundingMode.FLOOR)) >= 0) {
					order.setStatus(Order.Status.FILLED);
				} else {
					order.setStatus(Order.Status.PARTIALLY_FILLED);
				}
			}
		}
	}

	private void updatePrice(DefaultOrder order, double price, double tradedVolume) {
		if (tradedVolume == 0) {
			return;
		}
		double currentPrice = order.getPrice().doubleValue();
		double currentExecuted = order.getExecutedQuantity().doubleValue();

		double averagePrice = ((currentPrice * currentExecuted) + (price * tradedVolume)) / (currentExecuted + tradedVolume);

		order.setPrice(BigDecimal.valueOf(averagePrice));
	}

	private int getDecimals(double pips) {
		String tmp = new BigDecimal(pips, new MathContext(8, RoundingMode.HALF_EVEN)).toPlainString();
		int decimals = 0;

		boolean in = false;
		boolean started = false;

		for (int i = 1; i < tmp.length(); i++) {
			if (tmp.charAt(i) == '.') {
				in = true;
			} else if (in) {
				if (tmp.charAt(i) != '0') {
					started = true;
				} else if (tmp.charAt(i) == '0' && decimals > 0 && started) {
					if (i + 1 < tmp.length() && tmp.charAt(i + 1) == '0') {
						return decimals;
					}
				}
				decimals++;
			}

		}

		return decimals;
	}
}
