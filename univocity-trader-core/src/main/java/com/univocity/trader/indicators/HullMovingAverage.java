package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class HullMovingAverage extends SingleValueIndicator {

	private final WeightedMovingAverage halfWma;
	private final WeightedMovingAverage origWma;
	private final WeightedMovingAverage sqrtWma;

	public HullMovingAverage(TimeInterval interval) {
		this(9, interval);
	}

	public HullMovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public HullMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		halfWma = new WeightedMovingAverage(length / 2, interval, valueGetter);
		origWma = new WeightedMovingAverage(length, interval, valueGetter);
		sqrtWma = new WeightedMovingAverage((int) Math.sqrt(length), interval, c -> (halfWma.getValue() * 2) - origWma.getValue());
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (halfWma.accumulate(candle)) {
			origWma.accumulate(candle);
			sqrtWma.accumulate(candle);
			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return sqrtWma.getValue();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{halfWma, origWma, sqrtWma};
	}

}