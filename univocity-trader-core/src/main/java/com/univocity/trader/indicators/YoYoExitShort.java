package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;

public class YoYoExitShort extends ChandelierExitShort {

	public YoYoExitShort(TimeInterval interval) {
		this(22, interval, 3.0);
	}

	public YoYoExitShort(int length, TimeInterval interval, double k) {
		super(length, interval, k, c -> c.close);
	}
}