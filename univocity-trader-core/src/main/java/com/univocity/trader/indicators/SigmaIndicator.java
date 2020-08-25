package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class SigmaIndicator extends SingleValueCalculationIndicator {

	private final MovingAverage mean;
	private final StandardDeviation sd;

	public SigmaIndicator(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public SigmaIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval);
		valueGetter = valueGetter == null ? c -> c.close : valueGetter;
		mean = new MovingAverage(length, interval, valueGetter);
		sd = new StandardDeviation(length, interval, valueGetter);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if (mean.accumulate(candle)) {
			sd.accumulate(candle);
			return (candle.close - mean.getValue()) / sd.getValue();
		}
		return 0;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{mean, sd};
	}

}
