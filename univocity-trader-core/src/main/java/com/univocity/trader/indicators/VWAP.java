package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

public class VWAP extends SingleValueIndicator {

	private CircularList typicalPrice;
	private CircularList volume;
	private double value;

	public VWAP(int length, TimeInterval interval) {
		super(interval, c -> ((c.high + c.low + c.close) / 3.0) * c.volume);
		typicalPrice = new CircularList(length);
		volume = new CircularList(length);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (updating) {
			typicalPrice.update(value);
			volume.update(candle.volume);
		} else {
			typicalPrice.add(value);
			volume.add(candle.volume);
		}

		this.value = typicalPrice.sum() / Math.max(volume.sum(), 1.0);
		return true;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}