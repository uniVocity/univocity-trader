package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ModifiedMovingAverage extends SingleValueCalculationIndicator {

	private double multiplier;

	public ModifiedMovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public ModifiedMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
		multiplier = 1.0 / length;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if(getAccumulationCount() == 0){
			return value;
		}
		return ((value - previousValue) * multiplier) + previousValue;
	}



	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}