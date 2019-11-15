package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class HighestValueIndicator extends ValueSelectionIndicator {

	public HighestValueIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	@Override
	protected double select(double v1, double v2) {
		return Math.max(v1, v2);
	}

	@Override
	protected double initialValue() {
		return -9999999999999999999.9;
	}



	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
