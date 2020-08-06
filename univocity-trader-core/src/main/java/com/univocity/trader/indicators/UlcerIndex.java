package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class UlcerIndex extends MultiValueIndicator {

	private double value;

	public UlcerIndex(TimeInterval interval) {
		this(14, interval);
	}

	public UlcerIndex(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public UlcerIndex(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		final int size = values.size();
		int startIndex = values.getStartingIndex();
		int remaining = size;
		double squaredAverage = 0;

		double highestValue = values.get(startIndex);
		while (remaining-- > 0) {
			double currentValue = values.get(startIndex++);
			if (currentValue > highestValue) {
				highestValue = currentValue;
			}

			double percentageDrawdown = ((currentValue - highestValue) / highestValue) * 100.0;
			squaredAverage = squaredAverage + (percentageDrawdown * percentageDrawdown);
		}
		this.value = Math.sqrt(squaredAverage / size);
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
