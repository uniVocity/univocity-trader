package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class OBV extends SingleValueCalculationIndicator {


	private double previous;

	public OBV(TimeInterval interval) {
		super(interval);
	}

	@Override
	protected double calculate(Candle candle, double value, double obv, boolean updating) {
		if (getAccumulationCount() == 0) {
			previous = value;
			return 0.0;
		}

		if (value > previous) {
			obv = obv + candle.volume;
		} else if (value < previous) {
			obv = obv - candle.volume;
		}

		if (!updating) {
			previous = value;
		}

		return obv;
	}



	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
