package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class RateOfChange extends AbstractRateOfChange {

	public RateOfChange(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public RateOfChange(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter == null ? c -> c.close : valueGetter);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
