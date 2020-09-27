package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * A simple indicator whose value is determined by an input function. Allows creating arbitrary
 * indicators based on a {@link ToDoubleFunction}.
 */
public class FunctionIndicator extends SingleValueIndicator {

	private double value;

	public FunctionIndicator(TimeInterval timeInterval, ToDoubleFunction<Candle> function) {
		super(timeInterval, function);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[0];
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		this.value = value;
		return true;
	}

	@Override
	public double getValue() {
		return value;
	}
}
