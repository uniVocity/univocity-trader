package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class StochasticOscillatorD extends SingleValueIndicator {

	StochasticOscillatorK k;
	private MovingAverage sma;

	public StochasticOscillatorD(TimeInterval interval) {
		this(3, interval);
	}

	public StochasticOscillatorD(int dLength, TimeInterval interval) {
		this(dLength, dLength, interval);
	}

	public StochasticOscillatorD(int dLength, int kLength, TimeInterval interval) {
		super(interval, null);
		this.k = new StochasticOscillatorK(kLength, interval);
		this.sma = new MovingAverage(dLength, interval, null);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		k.accumulate(candle);
		if (updating) {
			sma.update(k.getValue());
		} else {
			sma.accumulate(k.getValue());
		}
		return true;
	}

	public double getValue() {
		return sma.getValue();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{k, sma};
	}
}
