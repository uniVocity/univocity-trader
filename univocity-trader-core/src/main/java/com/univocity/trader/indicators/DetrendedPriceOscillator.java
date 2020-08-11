package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class DetrendedPriceOscillator extends SingleValueIndicator {

	private double value;
	private MovingAverage ma;
	private CircularList timeShift;

	public DetrendedPriceOscillator(TimeInterval interval) {
		this(9, interval);
	}

	public DetrendedPriceOscillator(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public DetrendedPriceOscillator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		valueGetter = valueGetter == null ? c -> c.close : valueGetter;
		ma = new MovingAverage(length, interval, valueGetter);
		timeShift = new CircularList((length / 2) + 1);
	}


	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (ma.accumulate(candle)) {
			timeShift.accumulate(ma.getValue(), updating);
			this.value = candle.close - timeShift.first();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{ma};
	}

}
