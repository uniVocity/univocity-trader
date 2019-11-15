package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class StochasticOscillatorK extends SingleValueIndicator {

	private final LowestValueIndicator lows;
	private final HighestValueIndicator highs;
	private double value;

	public StochasticOscillatorK(TimeInterval interval) {
		this(14, interval);
	}

	public StochasticOscillatorK(int length, TimeInterval interval) {
		super(interval, null);
		this.lows = new LowestValueIndicator(length, interval, c -> c.low);
		this.highs = new HighestValueIndicator(length, interval, c -> c.high);
	}

	public double getValue() {
		return value;
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		lows.update(candle);
		highs.update(candle);

		double highestHigh = highs.getValue();
		double lowestLow = lows.getValue();
		this.value = ((candle.close - lowestLow) / (highestHigh - lowestLow)) * 100.0;

		return true;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{lows, highs};
	}
}
