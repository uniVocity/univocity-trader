package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import java.util.function.*;

public class StandardDeviation extends Variance {

	public StandardDeviation(TimeInterval interval) {
		this(4, interval);
	}

	public StandardDeviation(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public StandardDeviation(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (super.calculateIndicatorValue(candle, value, updating)) {
			super.value = Math.sqrt(super.getValue());
			return true;
		}
		return false;
	}
}