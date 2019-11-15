package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;

public class YoYoExitLong extends ChandelierExitLong {

	public YoYoExitLong(TimeInterval interval) {
		this(22, interval, 3.0);
	}

	public YoYoExitLong(int length, TimeInterval interval, double k) {
		super(length, interval, k, c -> c.close);
	}
}