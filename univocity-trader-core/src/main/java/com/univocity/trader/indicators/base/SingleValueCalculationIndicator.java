package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class SingleValueCalculationIndicator extends SingleValueIndicator {

	private double current;
	private double value;

	public SingleValueCalculationIndicator(TimeInterval interval) {
		this(interval, c -> c.close);
	}

	public SingleValueCalculationIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
	}

	@Override
	protected final boolean process(Candle candle, double value, boolean updating) {
		this.value = calculate(candle, value, this.current, updating);
		if (!updating) {
			this.current = this.value;
		}

		return true;
	}

	protected abstract double calculate(Candle candle, double value, double previousValue, boolean updating);

	@Override
	public double getValue() {
		return value;
	}

}