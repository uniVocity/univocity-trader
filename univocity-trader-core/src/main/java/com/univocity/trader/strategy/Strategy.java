package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

/**
 * A {@code Strategy} is responsible for processing price updates transmitted by an {@link com.univocity.trader.Exchange}
 * and generating a {@link Signal} which is then submitted to {@link Trader#trade(Candle, Signal, Strategy)}
 * to open, maintain or close a trade.
 *
 * {@code Strategy} implementations that make use of any {@link Indicator} implementation in package {@link com.univocity.trader.indicators}
 * should ideally extend from {@link IndicatorStrategy} for proper initialization.
 *
 * @see Signal
 * @see Trader
 * @see Indicator
 * @see IndicatorStrategy
 */
public interface Strategy {

	/**
	 * Processes the latest price update of a symbol to produce a {@link Signal}
	 *
	 * @param candle the latest candle received from a live {@link com.univocity.trader.Exchange} or the trading history of a symbol
	 *               (typically managed by {@link com.univocity.trader.simulation.SimulatedExchange}).
	 *
	 * @return an indication to {@code BUY}, {@code SELL} or do nothing (i.e. {@code NEUTRAL}). Any other value will be ignored by the
	 * {@link com.univocity.trader.account.Trader} that processes this {@link Signal}.
	 */
	Signal getSignal(Candle candle);

}
