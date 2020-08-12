package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class CoppockCurve extends SingleValueIndicator {

	private final RateOfChange longRoC;
	private final RateOfChange shortRoC;
	private final WeightedMovingAverage wma;

	public CoppockCurve(TimeInterval interval) {
		this(14, 11, 10, interval);
	}

	public CoppockCurve(int longRoCBarCount, int shortRoCBarCount, int wmaBarCount, TimeInterval interval) {
		super(interval, null);
		this.longRoC = new RateOfChange(longRoCBarCount, interval);
		this.shortRoC = new RateOfChange(shortRoCBarCount, interval);
		this.wma = new WeightedMovingAverage(wmaBarCount, interval, c -> longRoC.getValue() + shortRoC.getValue());
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (longRoC.accumulate(candle)) {
			shortRoC.accumulate(candle);
			wma.accumulate(candle);
			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return wma.getValue();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{wma, longRoC, shortRoC};
	}

}