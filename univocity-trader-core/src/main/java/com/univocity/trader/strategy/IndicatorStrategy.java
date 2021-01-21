package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

import java.util.*;

/**
 * A {@link Strategy} that uses any {@link Indicator} implementation (usually from package {@link com.univocity.trader.indicators})
 *
 * @see Strategy
 * @see Indicator
 * @see com.univocity.trader.indicators
 */
public abstract class IndicatorStrategy extends IndicatorGroup implements Strategy {

	/**
	 * Returns all indicators in this strategy, if any.
	 *
	 * @return a set of all indicators used by this strategy. Might be {@code null}.
	 */
	protected abstract Set<Indicator> getAllIndicators();

	/**
	 * Submits the latest price update of a symbol to this strategy and all of its indicators, in order to update their state
	 * and calculate a "final" {@link Signal}
	 *
	 * @param candle the latest candle received from a live {@link com.univocity.trader.Exchange} or the trading history of a symbol
	 *               (typically managed by {@link com.univocity.trader.simulation.SimulatedExchange}).
	 *
	 * @param context
	 *
	 * @return an indication to {@code BUY}, {@code SELL} or do nothing (i.e. {@code NEUTRAL}). Any other value will be ignored by the
	 * {@link com.univocity.trader.account.Trader} that processes this {@link Signal}.
	 */
	public abstract Signal getSignal(Candle candle, Context context);

}
