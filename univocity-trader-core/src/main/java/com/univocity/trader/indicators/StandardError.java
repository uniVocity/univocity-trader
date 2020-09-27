package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class StandardError extends StandardDeviation {

	public StandardError(TimeInterval interval) {
		this(5, interval);
	}

	public StandardError(int length, TimeInterval interval) {
		super(length, interval);
	}

    public StandardError(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
    }

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (super.calculateIndicatorValue(candle, value, updating)) {
			this.value = super.getValue() / Math.sqrt(values.size());
			return true;
		}
		return false;
	}


	@Override
	protected Indicator[] children() {
		return new Indicator[0];
	}

}
