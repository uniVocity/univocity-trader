package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class TypicalPrice extends SingleValueIndicator {

	private double value;

	public TypicalPrice(TimeInterval interval) {
		super(interval, null);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		this.value = (candle.high + candle.low + candle.close) / 3.0;
		return true;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}

}
