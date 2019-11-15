package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class DoubleExponentialMovingAverage extends ExponentialMovingAverage {

	private ExponentialMovingAverage ema;

	public DoubleExponentialMovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public DoubleExponentialMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
		ema = new ExponentialMovingAverage(length, interval, valueGetter);
	}

	@Override
	public void setAlpha(double alpha) {
		super.setAlpha(alpha);
		ema.setAlpha(alpha);
	}

	@Override
	protected double extractValue(Candle candle, boolean updating) {
		ema.update(candle);
		return ema.getValue();
	}

	@Override
	public double getValue() {
		double emaEma = super.getValue();
		return (2 * ema.getValue()) - emaEma;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{ema};
	}
}