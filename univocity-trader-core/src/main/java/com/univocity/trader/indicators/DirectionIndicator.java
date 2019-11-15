package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class DirectionIndicator<T extends SingleValueIndicator> implements Indicator {

	private ToDoubleFunction<T> valueGetter;
	private final T indicator;
	private final LinearRegression linearRegression = new LinearRegression();

	public DirectionIndicator(T indicator) {
		this(10, indicator);
	}

	public DirectionIndicator(int length, T indicator) {
		this(length, indicator, null);
	}

	public DirectionIndicator(T indicator, ToDoubleFunction<T> valueGetter) {
		this(10, indicator, valueGetter);
	}

	public DirectionIndicator(int length, T indicator, ToDoubleFunction<T> valueGetter) {
		this.valueGetter = valueGetter == null ? Indicator::getValue : valueGetter;
		this.indicator = indicator;
	}

	@Override
	public long getInterval() {
		return indicator.getInterval();
	}

	@Override
	public long getAccumulationCount() {
		return indicator.getAccumulationCount();
	}

	@Override
	public double getValue() {
		return indicator.getValue();
	}

	@Override
	public boolean accumulate(Candle candle) {
		if (indicator.accumulate(candle)) {
			linearRegression.add(valueGetter.applyAsDouble(indicator));
			return true;
		}
		return false;
	}

	public boolean accumulate(double value) {
		if (indicator.accumulate(value)) {
			linearRegression.add(valueGetter.applyAsDouble(indicator));
			return true;
		}
		return false;
	}

	public boolean update(double value) {
		if (indicator.update(value)) {
			linearRegression.update(valueGetter.applyAsDouble(indicator));
			return true;
		}
		return false;
	}

	@Override
	public boolean update(Candle candle) {
		if (indicator.update(candle)) {
			linearRegression.update(valueGetter.applyAsDouble(indicator));
			return true;
		}
		return false;
	}

	@Override
	public void initialize(Aggregator aggregator) {
		indicator.initialize(aggregator);
	}

	@Override
	public Signal getSignal(Candle candle) {
		return indicator.getSignal(candle);
	}

	public double predict(int next) {
		return linearRegression.predict(next);
	}

	public boolean goingUp() {
		return goingUp(1);
	}

	public boolean goingUp(int next) {
		return linearRegression.predict(next) > valueGetter.applyAsDouble(indicator);
	}

	public boolean goingDown() {
		return goingDown(1);
	}

	public boolean goingDown(int next) {
		return linearRegression.predict(next) > valueGetter.applyAsDouble(indicator);
	}

	public String toString() {
		return indicator.toString();
	}
}
