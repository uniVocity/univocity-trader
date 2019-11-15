package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class ChangeIndicator extends SingleValueIndicator {

	private double value;
	private double startingPoint;

	public ChangeIndicator(TimeInterval interval) {
		this(interval, c -> c.close);
	}

	public ChangeIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (getAccumulationCount() != 0) {
			this.value = ((value / startingPoint) - 1.0) * 100.0;
			if (!updating) {
				startingPoint = value;
			}
		} else {
			startingPoint = value;
		}

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
