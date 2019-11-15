package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class VolumeRateOfChange extends AbstractRateOfChange {

	public VolumeRateOfChange(int length, TimeInterval interval) {
		super(length, interval, c -> c.volume);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
