package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class MeanDeviation extends MultiValueIndicator {

	private double value;
	private final MovingAverage sma;

	public MeanDeviation(TimeInterval interval) {
		this(5, interval);
	}

	public MeanDeviation(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public MeanDeviation(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter == null ? c -> c.close : valueGetter);
		this.sma = new MovingAverage(length, interval);
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (sma.accumulate(candle)) {
			final double average = sma.getValue();

			final int count = this.values.size();
			int from = values.getStartingIndex();
			int c = count;

			double absoluteDeviations = 0;
			while (c-- > 0) {
                absoluteDeviations += Math.abs(values.get(from) - average);
				from = (from + 1) % count;
			}

			this.value = absoluteDeviations / count;
			return true;

		}
		return false;

	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{sma};
	}

}
