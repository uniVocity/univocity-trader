package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public abstract class SingleValueIndicator extends AggregatedTicksIndicator {

	private ToDoubleFunction<Candle> valueGetter;
	private Signal signal = null;

	public SingleValueIndicator(TimeInterval timeInterval, ToDoubleFunction<Candle> valueGetter) {
		super(timeInterval);
		this.valueGetter = valueGetter;
	}

	protected abstract Indicator[] children();

	protected abstract boolean process(Candle candle, double value, boolean updating);

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

	final boolean process(Candle candle, boolean updating) {
		double value = extractValue(candle, updating);
		if (process(candle, value, updating)) {
			signal = calculateSignal(candle);
			return true;
		}
		return false;
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
	public final Signal getSignal(Candle candle) {
		if (signal == null) {
			signal = calculateSignal(candle);
		}
		return signal;
	}

	protected Signal calculateSignal(Candle candle) {
		return Signal.NEUTRAL;
	}
}
