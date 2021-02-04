package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class StochasticOscillatorD extends SingleValueIndicator {

	public static final double UPPER_BOUND = 80.0;
	public static final double LOWER_BOUND = 20.0;
	private double upperBound = UPPER_BOUND;
	private double lowerBound = LOWER_BOUND;

	StochasticOscillatorK k;
	private MovingAverage sma;

	public StochasticOscillatorD(TimeInterval interval) {
		this(3, interval);
	}

	public StochasticOscillatorD(int dLength, TimeInterval interval) {
		this(dLength, 14, interval);
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

	public double k(){
		return k.getValue();
	}

	public double d(){
		return sma.getValue();
	}

	public double getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(double upperBound) {
		this.upperBound = upperBound;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(double lowerBound) {
		this.lowerBound = lowerBound;
	}
}
