package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MovingAverage extends MultiValueIndicator {

	private double value;

	public MovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public MovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		this.value = values.avg();
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
