package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class ChandelierExitShort extends SingleValueCalculationIndicator {

	private LowestValueIndicator low;
	private AverageTrueRange atr;
	private double k;

	public ChandelierExitShort(TimeInterval interval) {
		this(22, interval, 3.0);
	}

	public ChandelierExitShort(int length, TimeInterval interval, double k) {
		this(length, interval, k, c -> c.low);
	}

	ChandelierExitShort(int length, TimeInterval interval, double k, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		this.low = new LowestValueIndicator(length, interval, valueGetter);
		this.atr = new AverageTrueRange(length, interval);
		this.k = k;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		atr.update(candle);
		low.update(candle);
		return low.getValue() + (atr.getValue() * k);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{low, atr};
	}
}
