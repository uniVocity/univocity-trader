package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class RateOfChange extends AbstractRateOfChange {

	public RateOfChange(int length, TimeInterval interval) {
		super(length, interval, c -> c.close);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
