package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class EldersForceIndex extends SingleValueCalculationIndicator {

	private double previous;
	private SingleValueIndicator average;

	public EldersForceIndex(TimeInterval interval) {
		this(13, interval);
	}

	public EldersForceIndex(int length, TimeInterval interval) {
		super(interval);
		this.average = Indicators.ExponentialMovingAverage(length, interval);
	}

	public EldersForceIndex(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
		this.average = Indicators.ExponentialMovingAverage(length, interval);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		double efi = (value - previous) * candle.volume;
		if (!updating) {
			previous = value;
		}
		average.accumulate(efi);

		return average.getValue();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[0];
	}
}
