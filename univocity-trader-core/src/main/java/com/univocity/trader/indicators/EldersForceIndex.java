package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class EldersForceIndex extends SingleValueCalculationIndicator {

	private double previous;

	public EldersForceIndex(TimeInterval interval) {
		super(interval);
	}

	public EldersForceIndex(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		double efi = (value - previous) * candle.volume;
		if(!updating) {
			previous = value;
		}
		return efi;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[0];
	}
}
