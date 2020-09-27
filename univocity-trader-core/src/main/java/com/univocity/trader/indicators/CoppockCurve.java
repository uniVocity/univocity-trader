package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class CoppockCurve extends SingleValueIndicator {

	private final RateOfChange longRoC;
	private final RateOfChange shortRoC;
	private final WeightedMovingAverage wma;

	public CoppockCurve(TimeInterval interval) {
		this(interval, null);
	}

	public CoppockCurve(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		this(14, 11, 10, interval, valueGetter);
	}

	public CoppockCurve(int longRoCLength, int shortRoCLength, int wmaLength, TimeInterval interval) {
		this(longRoCLength, shortRoCLength, wmaLength, interval, null);
	}

	public CoppockCurve(int longRoCLength, int shortRoCLength, int wmaLength, TimeInterval interval,  ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		valueGetter = valueGetter == null ? c -> c.close : valueGetter;
		this.longRoC = new RateOfChange(longRoCLength, interval, valueGetter);
		this.shortRoC = new RateOfChange(shortRoCLength, interval, valueGetter);
		this.wma = new WeightedMovingAverage(wmaLength, interval, c -> longRoC.getValue() + shortRoC.getValue());
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