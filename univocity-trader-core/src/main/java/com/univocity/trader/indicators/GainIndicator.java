package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class GainIndicator extends SingleValueCalculationIndicator {

	private double prev = -1;

	public GainIndicator(TimeInterval interval) {
		this(interval, null);
	}

	public GainIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter == null ? c -> c.close : valueGetter);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if (prev == -1) {
			prev = value;
			return 0;
		}
		double out = 0;
		if (value > prev) {
			out = value - prev;
		}
		prev = value;
		return out;

	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}

}
