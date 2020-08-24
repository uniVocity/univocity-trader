package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

public class AggregatedTicksIndicator implements Indicator {

	private boolean testing = false;

	private final TimeInterval timeInterval;
	private Aggregator aggregator;
	long accumulationCount;
	private boolean recalculateEveryTick = false;
	private Candle lastFullCandle;

	public AggregatedTicksIndicator(TimeInterval timeInterval) {
		this.timeInterval = timeInterval;
	}

	public Aggregator getAggregator() {
		return aggregator;
	}

	@Override
	public final void initialize(Aggregator root) {
		if (this.aggregator == null) {
			aggregator = root.getInstance(timeInterval);
			for (Indicator indicator : children()) {
				indicator.initialize(root);
			}
		}
	}

	protected Indicator[] children() {
		return new Indicator[0];
	}

	@Override
	public final boolean accumulate(Candle candle) {
		if (aggregator == null) {
			try {
				throw new IllegalStateException(getClass().getSimpleName() + " not properly initialized. Ensure nested indicators are returned in method `protected Indicator[] children()`");
			} catch (IllegalStateException e) {
				StackTraceElement[] s = Thread.currentThread().getStackTrace();
				for (StackTraceElement ste : s) {
					if (ste.toString().contains("junit")) {
						aggregator = new Aggregator("fakeTestRoot").getInstance(timeInterval);
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
			if (candle != null) {
				return process(candle, true);
			}
		}
		candle = aggregator.getFull();
		if (candle != null) {
			lastFullCandle = candle;
			if (process(candle, false)) {
				accumulationCount++;
				return true;
			}
		}
		return false;
	}

	boolean process(Candle candle, boolean updating) {
		return true;
	}

	public final Candle getLastFullCandle() {
		if (lastFullCandle == null) {
			return aggregator.getPartial();
		}
		return lastFullCandle;
	}

	public final Candle getCandle() {
		Candle partial = aggregator.getPartial();
		if (partial == null) {
			return aggregator.getFull();
		}
		return partial;
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
	public Signal getSignal(Candle candle) {
		return Signal.NEUTRAL;
	}

	public String toString() {
		return timeInterval + "_" + getClass().getSimpleName();
	}

	public final boolean recalculateEveryTick() {
		return recalculateEveryTick;
	}

	@Override
	public double getValue() {
		return 0;
	}

	public final void recalculateEveryTick(boolean recalculateEveryTick) {
		this.recalculateEveryTick = recalculateEveryTick;
		for (Indicator indicator : children()) {
			if (indicator instanceof AggregatedTicksIndicator) {
				indicator.recalculateEveryTick(recalculateEveryTick);
			}
		}
	}
}
