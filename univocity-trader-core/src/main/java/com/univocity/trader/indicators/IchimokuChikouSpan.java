package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class IchimokuChikouSpan extends SingleValueIndicator {

	private CircularList list;
	private double value;

	public IchimokuChikouSpan(TimeInterval interval) {
		this(26, interval);
	}

	public IchimokuChikouSpan(int timeDelay, TimeInterval interval) {
		this(timeDelay, interval, null);
	}

	public IchimokuChikouSpan(int timeDelay, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter == null ? c -> c.close : valueGetter);
		this.list = new CircularList(timeDelay);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		list.accumulate(value, updating);
		this.value = list.first();
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
