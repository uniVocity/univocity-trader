package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class KeltnerChannel extends SingleValueCalculationIndicator {

	private final ExponentialMovingAverage middle;
	private final AverageTrueRange atr;

	public KeltnerChannel(TimeInterval interval) {
		this(20, 10, interval, null);
	}

	public KeltnerChannel(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		this(20, 10, interval, valueGetter);
	}

	public KeltnerChannel(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public KeltnerChannel(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		this(length, 10, interval, valueGetter);
	}

	public KeltnerChannel(int length, int atrLength, TimeInterval interval) {
		this(length, atrLength, interval, null);
	}

	public KeltnerChannel(int length, int atrLength, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter != null ? valueGetter : c -> (c.high + c.low + c.close) / 3.0);
		this.middle = new ExponentialMovingAverage(length, interval);
		this.atr = new AverageTrueRange(atrLength, interval);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		atr.update(candle);
		if(updating) {
			middle.update(value);
		} else {
			middle.accumulate(value);
		}
		return middle.getValue();
	}

	public double getUpperBand() {
		double atr = this.atr.getValue();
		double middle = getMiddleBand();
		return middle + (2.0 * atr);
	}

	public double getLowerBand() {
		double atr = this.atr.getValue();
		double middle = getMiddleBand();
		return middle - (2.0 * atr);
	}

	public double getMiddleBand() {
		return getValue();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{atr, middle};
	}
}
