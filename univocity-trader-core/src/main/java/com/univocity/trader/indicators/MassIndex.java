package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

public class MassIndex extends SingleValueIndicator {

	private final CircularList sum;
	private final ExponentialMovingAverage singleEma;
	private final ExponentialMovingAverage doubleEma;

	public MassIndex(TimeInterval interval) {
		this(25, interval);
	}

	public MassIndex(int length, TimeInterval interval) {
		this(9, length, interval);
	}

	public MassIndex(int emaBarCount, int length, TimeInterval interval) {
		super(interval, null);
		singleEma = new ExponentialMovingAverage(emaBarCount, interval, candle -> candle.high - candle.low);
		doubleEma = new ExponentialMovingAverage(emaBarCount, interval, c -> singleEma.getValue());
		this.sum = new CircularList(length);

	}

	@Override
	public double getValue() {
		return sum.sum();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{singleEma, doubleEma};
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if(singleEma.accumulate(candle)) {
			doubleEma.accumulate(candle);

			sum.add(singleEma.getValue() / doubleEma.getValue());

			return true;
		}
		return false;
	}
}
