package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class Volume extends MultiValueIndicator {

    private boolean upside;
    
	public Volume(TimeInterval interval) {
		this(1, interval);
	}

	public Volume(int length, TimeInterval interval) {
		this(length, interval, c -> c.volume);
	}

	public Volume(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
	    upside = candle.close > candle.open;
	    return true;
	}

	public boolean isMoveToUpside() {
        return upside;
	}

	@Override
	public double getValue() {
		return values.sum();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}

}
