package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class WilliamsRIndicator extends SingleValueIndicator {

	private double value;

	private HighestValueIndicator highestHigh;
	private LowestValueIndicator lowestMin;

	public WilliamsRIndicator(TimeInterval interval) {
		this(14, interval);
	}

	public WilliamsRIndicator(int length, TimeInterval interval) {
		super(interval, null);
		highestHigh = new HighestValueIndicator(length, interval, c -> c.high);
		lowestMin = new LowestValueIndicator(length, interval, c -> c.low);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (highestHigh.accumulate(candle)) {
			double highestHighPrice = highestHigh.getValue();

			lowestMin.accumulate(candle);
			double lowestLowPrice = lowestMin.getValue();

			this.value = ((highestHighPrice - candle.close) / (highestHighPrice - lowestLowPrice)) * -100;
			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{highestHigh, lowestMin};
	}

}