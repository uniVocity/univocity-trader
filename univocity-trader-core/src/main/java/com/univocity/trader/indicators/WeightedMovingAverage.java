package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class WeightedMovingAverage extends MultiValueIndicator {

	private final double length;
	private double value;

	public WeightedMovingAverage(TimeInterval interval) {
		this(3, interval);
	}

	public WeightedMovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public WeightedMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
		this.length = length;
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		final int count = values.size();
		double divisor = count < length ? count : length;
		divisor = ((divisor * (divisor + 1.0)) / 2.0);

		int i = 1;
		int from = values.getStartingIndex();
		int c = count;
		value = 0.0;
		while (c-- > 0) {
			value += values.get(from) * i++;
			from = (from + 1) % count;
		}
		this.value = value / divisor;


		return true;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
