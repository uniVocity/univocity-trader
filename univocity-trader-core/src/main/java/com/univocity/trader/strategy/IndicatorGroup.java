package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;

import java.util.*;

/**
 * A group of {@link Indicator} used to assist in creating a {@link Strategy} efficiently. All indicators in an {@link IndicatorGroup} share
 * a common {@link Aggregator} which is responsible for merging higher resolution candles into a lower resolution {@link Candle}
 * (e.g. merging five 1-minute candles into a single 5-minutes candle) so any indicator of the group that works in the same time frame
 * will receive the same merged candle.
 *
 * @see IndicatorStrategy
 * @see StrategyMonitor
 * @see Indicator
 * @see Aggregator
 */
public abstract class IndicatorGroup {

	private Indicator[] indicators;

	/**
	 * Initializes all indicators of this group (returned via {@link #getAllIndicators()}) to use the same {@link Aggregator}
	 *
	 * @param parent the aggregator to be used by all indicators in this group
	 */
	final void initialize(Aggregator parent) {
		if (indicators != null) {
			return;
		}
		Set<Indicator> allIndicators = getAllIndicators();
		if (allIndicators == null) {
			indicators = new Indicator[0];
			return;
		}

		indicators = allIndicators.toArray(new Indicator[0]);
		for (int i = 0; i < indicators.length; i++) {
			indicators[i].initialize(parent);
		}
	}

	/**
	 * Cycles through all indicators in this group and invokes their {@link Indicator#accumulate(Candle)} method, so they can update their state.
	 *
	 * After all indicators are updated, method {@link #candleAccumulated(Candle)} will be invoked to notify implementations of this class
	 * that state of the indicators in this group might have changed.
	 *
	 * @param candle the latest price details returned by an {@link com.univocity.trader.Exchange}
	 */
	public final void accumulate(Candle candle) {
		for (int i = 0; i < indicators.length; i++) {
			indicators[i].accumulate(candle);
		}
		candleAccumulated(candle);
	}

	/**
	 * Callback method used to notify subclasses that a {@link Candle} was accumulated and the indicators of this group might have a new state.
	 * Does nothing by default.
	 *
	 * @param candle the latest price details returned by an {@link com.univocity.trader.Exchange}
	 */
	protected void candleAccumulated(Candle candle) {

	}

	/**
	 * Returns all indicators in this group, if any.
	 *
	 * @return a set of all indicators in this group. Might be {@code null}.
	 */
	protected abstract Set<Indicator> getAllIndicators();
}
