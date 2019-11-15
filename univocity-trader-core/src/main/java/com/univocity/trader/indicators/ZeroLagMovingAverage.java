package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class ZeroLagMovingAverage extends SingleValueCalculationIndicator {

	private final double k;
	private final int lag;
	private final CircularList tmp;

	public ZeroLagMovingAverage(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public ZeroLagMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);

		k = 2.0 / (length + 1);
		lag = ((length - 1) / 2);
		tmp = new CircularList(length);
	}

	@Override
	protected double extractValue(Candle candle, boolean updating) {
		double value = super.extractValue(candle, updating);
		if (updating) {
			tmp.update(value);
		} else {
			tmp.add(value);
		}
		return value;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if (getAccumulationCount() == 0) {
			return value;
		}
		double priceLag = tmp.getRecentValue((int)Math.min(lag, getAccumulationCount()));
		return (k * ((2 * value) - priceLag)) + ((1 - k) * previousValue);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
