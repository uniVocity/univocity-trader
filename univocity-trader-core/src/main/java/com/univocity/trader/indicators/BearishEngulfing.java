package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class BearishEngulfing extends SingleValueIndicator {

	private double value;
	private Candle prev;

	public BearishEngulfing(TimeInterval interval) {
		super(interval, null);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		this.value = 0;
		if (prev != null && prev.isClosePositive() && !candle.isClosePositive()) {
			this.value = (candle.open > prev.open && candle.open > prev.close && candle.close < prev.open && candle.close < prev.close) ? 1 : 0;
		}
		if (!updating || prev == null) {
			prev = candle;
		}
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
