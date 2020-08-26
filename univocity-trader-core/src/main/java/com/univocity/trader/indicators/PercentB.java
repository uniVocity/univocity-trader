package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class PercentB extends SingleValueCalculationIndicator {

	private final BollingerBand bb;

	public PercentB(TimeInterval interval) {
		this(5, 2, interval, null);
	}

	public PercentB(int length, double multiplier, TimeInterval interval) {
		this(length, multiplier, interval, null);
	}

	public PercentB(int length, double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval);
		this.bb = new BollingerBand(length, multiplier, interval, valueGetter);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if (this.bb.accumulate(candle)) {
			return (value - bb.getLowerBand()) / (bb.getUpperBand() - bb.getLowerBand());
		}
		return 0;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{bb};
	}
}
