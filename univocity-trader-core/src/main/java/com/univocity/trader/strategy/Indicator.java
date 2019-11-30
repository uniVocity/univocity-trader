package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

/**
 * An {@code Indicator} typically performs calculations to produce values and/or trading signals based on the history of
 * price movements of a given instrument.
 *
 * Common technical indicator implementations are available in package {@link com.univocity.trader.indicators})
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 * @see com.univocity.trader.indicators
 * @see Candle
 * @see Signal
 * @see Strategy
 * @see StrategyMonitor
 */
public interface Indicator {

	/**
	 * Attempts to modify the internal state of this indicator using the given candle. If configured with {@link #recalculateEveryTick(boolean)} set to
	 * {@code false} the candle must only be considered only if the time between the previous {@link Candle#closeTime} and the current {@link Candle#closeTime}
	 * is equal to or greater than the interval specified by {@link #getInterval()}.
	 *
	 * For example, a moving average set to an interval of 2 minutes, receiving 1-minute candles, will be calculated in the following manner:
	 *
	 * - Minute 1, candle(1.0) =      [0.0]. Average: 0.0 (ignored)
	 * - Minute 2, candle(1.5) =      [1.5]. Average: 1.5
	 * - Minute 3, candle(1.2) =      [1.5]. Average: 1.5 (ignored)
	 * - Minute 4, candle(1.3) = [1.5, 1.3]. Average: 1.4
	 *
	 * When the indicator is configured with {@link #recalculateEveryTick(boolean)} set to {@code true}, then the indicator state will be recalculated
	 * based on all accumulated values collected at every interval defined by {@link #getInterval()}, plus the given candle state:
	 *
	 * - Minute 1, candle(1.0) =      [1.0]. Average :1.0
	 * - Minute 2, candle(1.5) =      [1.5]. Average: 1.5
	 * - Minute 3, candle(1.2) = [1.5, 1.2]. Average :1.35
	 * - Minute 4, candle(1.3) = [1.5, 1.3]. Average :1.4
	 *
	 * @param candle the latest candle received from a live {@link com.univocity.trader.Exchange} or the trading history of a symbol
	 *               (typically managed by {@link com.univocity.trader.simulation.SimulatedExchange}).
	 *
	 * @return {@code true} if the candle was processed by this indicator, or {@code false} if it was ignored.
	 */
	boolean accumulate(Candle candle);

	/**
	 * Returns the number of candles accumulated so far, collected at every interval defined by {@link #getInterval()}. Intermediate candles that
	 * might have been processed when {@link #recalculateEveryTick(boolean)} set to {@code true} should not be accounted for.
	 *
	 * @return the number of accumulated candles by this indicator.
	 */
	long getAccumulationCount();

	/**
	 * Returns the value of this indicator, if applicable.
	 *
	 * @return the indicator value.
	 */
	double getValue();

	/**
	 * Returns the interval this indicator works at, e.g. calculates values of 5-second candles, 1-hour candles, etc
	 *
	 * @return the time interval used by this indicator, in milliseconds.
	 */
	long getInterval();

	/**
	 * Emits a signal based on the current indicator state and the given candle. Usually defaults to {@code NEUTRAL} as the calculation
	 * depends heavily on the user. For example, a {@link BollingerBand} might return a {@code BUY} if the closing price of the given candle
	 * is less than the {@link BollingerBand#getLowerBand()}. Users are expected to override this method to determine which signals make
	 * sense to them.
	 *
	 * @param candle the latest candle received from a live {@link com.univocity.trader.Exchange} or the trading history of a symbol
	 *               (typically managed by {@link com.univocity.trader.simulation.SimulatedExchange}).
	 *
	 * @return the current indicator signal, if applicable, or {@code NEUTRAL}
	 */
	Signal getSignal(Candle candle);

	/**
	 * Initializes this indicator with an {@link Aggregator} which will merge resolution candles into candles that contain data for the
	 * interval specified by {@link #getInterval()}.
	 *
	 * @param aggregator the aggregator instance to be used and shared among other indicator instances.
	 */
	default void initialize(Aggregator aggregator) {
		throw new IllegalStateException("method initialize(aggregator) must be implemented in " + this.getClass().getSimpleName());
	}

	/**
	 * Configures this indicator to react to every tick, i.e. candles partially processed by the given {@link Aggregator}, which are returned
	 * via {@link Aggregator#getPartial()} and whose values are still being merged to form a full candle of the specified interval given by
	 * {@link #getInterval()}. If {@code false}, then only candles coming from {@link Aggregator#getFull()} will be considered by this indicator.
	 *
	 * For example, a moving average set to an interval of 2 minutes, receiving 1-minute candles, will be calculated in the following manner:
	 *
	 * - Minute 1, candle(1.0) =      [0.0]. Average: 0.0 (ignored)
	 * - Minute 2, candle(1.5) =      [1.5]. Average: 1.5 (full)
	 * - Minute 3, candle(1.2) =      [1.5]. Average: 1.5 (ignored)
	 * - Minute 4, candle(1.3) = [1.5, 1.3]. Average: 1.4 (full)
	 *
	 * When the indicator is configured with {@link #recalculateEveryTick(boolean)} set to {@code true}, then the indicator state will be recalculated
	 * based on all accumulated values collected at every interval defined by {@link #getInterval()}, plus the given candle coming
	 * from {@link Aggregator#getPartial()}:
	 *
	 * - Minute 1, candle(1.0) =      [1.0]. Average :1.0  (partial)
	 * - Minute 2, candle(1.5) =      [1.5]. Average: 1.5  (full)
	 * - Minute 3, candle(1.2) = [1.5, 1.2]. Average :1.35 (partial)
	 * - Minute 4, candle(1.3) = [1.5, 1.3]. Average :1.4  (full)
	 *
	 * @param recalculateEveryTick flag indicating whether or not values of partial candles (i.e. candles that do not fully represent the entire price movement
	 *                             during the interval of this indicator) should be used to calculate this indicator value/signal
	 */
	default void recalculateEveryTick(boolean recalculateEveryTick) {

	}
}
