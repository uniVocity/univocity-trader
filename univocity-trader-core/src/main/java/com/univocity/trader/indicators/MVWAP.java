package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class MVWAP extends SingleValueIndicator {

	private MovingAverage sma;
	private VWAP vwap;
	private double value;

	public MVWAP(int length, int vwapLength, TimeInterval interval) {
		super(interval, null);
		vwap = new VWAP(vwapLength, interval);
		sma = new MovingAverage(length, interval);
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		vwap.update(candle);
		if (updating) {
			sma.update(vwap.getValue());
		} else {
			sma.accumulate(vwap.getValue());
		}
		this.value = sma.getValue();
		return true;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{sma, vwap};
	}
}