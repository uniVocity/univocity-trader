package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public abstract class Statistic implements Indicator {

	protected final int length;
	protected final long interval;
	private long count;
	private double value;

	public Statistic(int length, TimeInterval interval, ToDoubleFunction<Candle> indicator1, ToDoubleFunction<Candle> indicator2) {
		this(length, new FunctionIndicator(interval, indicator1), new FunctionIndicator(interval, indicator2));
	}

	public Statistic(int length, Indicator indicator1, ToDoubleFunction<Candle> indicator2) {
		this(length, indicator1, new FunctionIndicator(TimeInterval.millis(indicator1.getInterval()), indicator2));
	}

	public Statistic(int length, ToDoubleFunction<Candle> indicator1, Indicator indicator2) {
		this(length, new FunctionIndicator(TimeInterval.millis(indicator2.getInterval()), indicator1), indicator1);
	}

	public Statistic(int length, Indicator indicator1, Indicator indicator2) {
		this.length = length;
		this.interval = Math.min(indicator1.getInterval(), indicator2.getInterval());
		initialize(indicator1, indicator2);

	}

	protected abstract void initialize(Indicator indicator1, Indicator indicator2);

	protected abstract boolean indicatorsAccumulated(Candle candle);

	protected abstract double calculate();

	@Override
	public final boolean accumulate(Candle candle) {
		if (indicatorsAccumulated(candle)) {
			count++;
			this.value = calculate();
			return true;
		}
		return false;
	}

	@Override
	public final double getValue() {
		return value;
	}

	@Override
	public final long getAccumulationCount() {
		return count;
	}

	@Override
	public final long getInterval() {
		return interval;
	}

	@Override
	public final Signal getSignal(Candle candle) {
		return Signal.NEUTRAL;
	}
}
