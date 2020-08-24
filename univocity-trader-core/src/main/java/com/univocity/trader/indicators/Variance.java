package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import java.util.function.*;

public class Variance extends MovingAverage {

	protected double value;

	public Variance(TimeInterval interval) {
		this(4, interval);
	}

	public Variance(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public Variance(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter == null ? c -> c.close : valueGetter);
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (super.calculateIndicatorValue(candle, value, updating)) {
			final double average = super.getValue();

			final int count = this.values.size();
			int from = values.getStartingIndex();
			int c = count;

			double variance = 0;
			while (c-- > 0) {
				double v = values.get(from) - average;
				variance += v * v;
				from = (from + 1) % count;
			}

			this.value = variance / count;
			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return this.value;
	}
}
