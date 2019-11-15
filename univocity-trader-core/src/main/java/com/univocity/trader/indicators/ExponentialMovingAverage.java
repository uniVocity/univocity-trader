package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ExponentialMovingAverage extends SingleValueCalculationIndicator {

	private double alpha;


	public ExponentialMovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public ExponentialMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
		alpha = 2.0 / ((double) length + 1.0);
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if(getAccumulationCount() == 0){
			return value;
		}
		return previousValue + alpha * (value - previousValue);
	}



	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
