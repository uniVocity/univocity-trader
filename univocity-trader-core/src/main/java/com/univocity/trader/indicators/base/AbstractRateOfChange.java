package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public abstract class AbstractRateOfChange extends SingleValueIndicator {

	private final CircularList values;
	private double value;

	public AbstractRateOfChange(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
		this.values = new CircularList(length + 1);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (updating) {
			values.update(value);
		} else {
			values.add(value);
		}

		double oldValue = values.getRecentValue(values.size());
		if(oldValue == 0.0){
			return false;
		}
		this.value = ((value - oldValue) / oldValue) * 100.0;

		return true;
	}

	@Override
	public double getValue() {
		return value;
	}


}
