package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class PVT extends SingleValueCalculationIndicator {

	private double previousValue;

	public PVT(TimeInterval interval) {
		super(interval, c -> c.close);
	}

	public PVT(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousPvt, boolean updating) {
		if (getAccumulationCount() == 0) {
			previousValue = value;
			return 0.0;
		}

		double out = (((value - previousValue) / previousValue) * candle.volume) + previousPvt;

		if (!updating) {
			previousValue = value;
		}

		return out;
	}



	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
