package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;

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
		double quantity = order.getQuantity();
		double executed = order.getExecutedQuantity();
		if (executed >= quantity) {
			order.setStatus(Order.Status.FILLED);
			return;
		}

		double pips = (candle.high - candle.low);
		double decimals = Utils.countDecimals(pips);
		double multiplier = (Math.pow(10, decimals));

		double increment = 1.0 / multiplier;
		double totalVolume = candle.volume;
		if (totalVolume <= 0 && candle.isTick()) {
			throw new IllegalStateException("Cannot emulate slippage on candles without volume information. Configure simulation to use `fillOrdersOnPriceMatch()` instead of `emulateSlippage()`.");
		}

		if (totalVolume == 0 && order.isMarket()) {
			pips = 10;
			totalVolume = order.getQuantity();
		}

		double volumePerPip = pips == 0 ? totalVolume : totalVolume / pips;

		final boolean buying = order.isBuy();

		double price = order.getPrice();
		double high = candle.high;
		double low = candle.low;
		if (order.isMarket()) { //market order, let it run until order fills
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

		increment = order.isMarket() ? -increment : increment;

		if (order.isLimit() || order.isMarket()) {
			while (totalVolume > 0 && quantity > 0 && ((buying && price >= low) || (!buying && price <= high))) {
				totalVolume -= volumePerPip;
				quantity -= volumePerPip;
				if (totalVolume < 0 || quantity < 0) {
					quantity = volumePerPip - Math.abs(Math.min(totalVolume, quantity));

					//check if fully filled with 1% disparity against original quantity (caused by precision errors)
					if (Math.abs(1 - (order.getQuantity() / (executed + quantity))) * 100.0 < 1.0) {
						quantity = order.getQuantity() - executed;
						executed = order.getQuantity();
					} else {
						executed += quantity;
					}

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

			if (order.isMarket()) {
				double averagePrice = totalPaid / tradedVolume;
				averagePrice = (averagePrice + candle.open + ((candle.open + candle.close + (buying ? candle.high : candle.low)) / 3.0)) / 3.0;
				if (buying && averagePrice > candle.high) {
					if (candle.volume != 0) {
						averagePrice = candle.high;
					} else {
						averagePrice = Math.min(averagePrice, candle.high * 1.015);
					}
				} else if (!buying && averagePrice < candle.low) {
					if (candle.volume != 0) {
						averagePrice = candle.low;
					} else {
						averagePrice = Math.max(averagePrice, candle.low * 0.985);
					}
				}
				updatePrice(order, averagePrice, tradedVolume);
			} else if (order.isLimit()) {
				if (order.isBuy() && candle.high < order.getPrice()) {
					updatePrice(order, candle.high, tradedVolume);
				} else if (order.isSell() && candle.low > order.getPrice()) {
					updatePrice(order, candle.low, tradedVolume);
				} else {
					updatePrice(order, -1, tradedVolume);
				}
			}
			order.setExecutedQuantity(executed);
			if (order.getExecutedQuantity() > 0) {
				if (order.getExecutedQuantity() >= order.getQuantity()) {
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
		double currentPrice = order.getPrice();
		double currentExecuted = order.getExecutedQuantity();

		if (price == -1) {
			price = order.getPrice();
		}

		double averagePrice = (((currentPrice * currentExecuted) + (price * tradedVolume)) / (currentExecuted + tradedVolume));
		if (order.isMarket()) {
			order.setPrice(averagePrice);
		}
		order.setAveragePrice(averagePrice);
		order.setPartialFillDetails((tradedVolume), (price));
	}

}
