package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class StreakIndicator extends SingleValueCalculationIndicator {

	private int streak;

	public StreakIndicator(TimeInterval interval) {
		this(interval, c -> c.close);
	}

	public StreakIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
	}

	@Override
	public double getValue() {
		return streak;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if(getAccumulationCount() == 0){
			streak = 0;
		} else {
			if (value == previousValue) {
				streak = 0;
			} else if (value < previousValue) {
				if(streak > 0){
					streak = 0;
				}
				streak--;
			} else if (value > previousValue) {
				if(streak < 0){
					streak = 0;
				}
				streak++;
			}
		}
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
