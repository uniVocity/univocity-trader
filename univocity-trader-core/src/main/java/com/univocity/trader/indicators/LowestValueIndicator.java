package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class LowestValueIndicator extends ValueSelectionIndicator {

	public LowestValueIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	@Override
	protected double select(double v1, double v2) {
		return Math.min(v1, v2);
	}

	@Override
	protected double initialValue() {
		return Double.MAX_VALUE;
	}



	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
