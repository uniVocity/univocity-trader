package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public abstract class SingleValueIndicator implements Indicator {

	private boolean testing = false;

	private final TimeInterval timeInterval;
	private Aggregator aggregator;
	private long accumulationCount;
	private ToDoubleFunction<Candle> valueGetter;
	private boolean recalculateEveryTick = false;

	private Signal signal = null;

	public SingleValueIndicator(TimeInterval timeInterval, ToDoubleFunction<Candle> valueGetter) {
		this.timeInterval = timeInterval;
		this.valueGetter = valueGetter;
	}

	@Override
	public final void initialize(Aggregator root) {
		if (this.aggregator == null) {
			aggregator = Aggregator.getInstance(root, timeInterval);
			for (Indicator indicator : children()) {
				indicator.initialize(root);
			}
		}
	}

	protected abstract Indicator[] children();

	protected abstract boolean process(Candle candle, double value, boolean updating);

	@Override
	public final boolean accumulate(Candle candle) {
		return update(candle);
	}

	@Override
	public final boolean update(Candle candle) {
		if (aggregator == null) {
			try {
				throw new IllegalStateException(getClass().getSimpleName() + " not properly initialized. Ensure nested indicators are returned in method `protected Indicator[] children()`");
			} catch (IllegalStateException e) {
				StackTraceElement[] s = Thread.currentThread().getStackTrace();
				for (StackTraceElement ste : s) {
					if (ste.toString().contains("junit")) {
						aggregator = Aggregator.getInstance(new Aggregator("fakeTestRoot"), timeInterval);
						testing = true;
					}
				}
				if (aggregator == null) {
					throw e;
				}
			}
		}
		if (testing) {
			aggregator.aggregate(candle);
		}

		if (recalculateEveryTick) {
			candle = aggregator.getPartial();
			if (candle != null && process(candle, true)) {
				signal = null;
				getSignal(candle);
				return true;
			}
		}
		candle = aggregator.getFull();
		if (candle != null && process(candle, false)) {
			signal = null;
			accumulationCount++;
			getSignal(candle);
			return true;
		}
		return false;
	}

	/**
	 * Override me!
	 *
	 * @param candle
	 * @param updating
	 *
	 * @return
	 */
	protected double extractValue(Candle candle, boolean updating) {
		if (valueGetter != null) {
			return valueGetter.applyAsDouble(candle);
		}
		return 0.0;
	}

	private boolean process(Candle candle, boolean updating) {
		double value = extractValue(candle, updating);
		return process(candle, value, updating);
	}

	public final boolean accumulate(double value) {
		if (process(null, value, false)) {
			accumulationCount++;
			return true;
		}
		return false;
	}

	public final boolean update(double value) {
		return process(null, value, true);
	}

	@Override
	public final long getInterval() {
		return timeInterval.ms;
	}

	@Override
	public final long getAccumulationCount() {
		return accumulationCount;
	}

	@Override
	public final Signal getSignal(Candle candle) {
		if (signal == null) {
			signal = calculateSignal(candle);
		}
		return signal;
	}

	protected Signal calculateSignal(Candle candle) {
		return Signal.NEUTRAL;
	}

	public String toString() {
		return timeInterval + "_" + getClass().getSimpleName();
	}

	public final boolean recalculateEveryTick() {
		return recalculateEveryTick;
	}

	public final void recalculateEveryTick(boolean recalculateEveryTick) {
		this.recalculateEveryTick = recalculateEveryTick;
		for (Indicator indicator : children()) {
			if (indicator instanceof SingleValueIndicator) {
				indicator.recalculateEveryTick(recalculateEveryTick);
			}
		}
	}
}
