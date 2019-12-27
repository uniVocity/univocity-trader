package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

/**
 * Controls how an {@link Order} is should filled (in {@link SimulatedClientAccount#fillOrder}) when running a simulation.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum OrderFillEmulation {

	/**
	 * Fills new orders immediately using the closing price of the current candle
	 */
	IMMEDIATE,

	/**
	 * If the {@link Order.Type} is {@code LIMIT}, fills the order when its price is within the high/low of a future candle
	 *
	 * If the {@link Order.Type} is {@code MARKET}, fills the order using the average price of the next candle.
	 * The average price depends on the {@link Order.Side}:
	 *
	 * <ul>
	 *     <li>{@code BUY}: average = (open + close + high / 3)</li>
	 *     <li>{@code SELL}: average = (open + close + low / 3)</li>
	 * </ul>
	 *
	 * Orders will be always 100% filled once a matching candle is found.
	 */
	PRICE_MATCH,

	/**
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
	SLIPPAGE
}
