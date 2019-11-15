package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class ChandelierExitLong extends SingleValueCalculationIndicator {

	private HighestValueIndicator high;
	private AverageTrueRange atr;
	private double k;

	public ChandelierExitLong(TimeInterval interval) {
		this(22, interval, 3.0);
	}

	public ChandelierExitLong(int length, TimeInterval interval, double k) {
		this(length, interval, k, c -> c.high);
	}

	ChandelierExitLong(int length, TimeInterval interval, double k, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		this.high = new HighestValueIndicator(length, interval, valueGetter);
		this.atr = new AverageTrueRange(length, interval);
		this.k = k;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		high.update(candle);
		atr.update(candle);
		return high.getValue() - (atr.getValue() * k);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{high, atr};
	}
}
