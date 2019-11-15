package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class MultiValueIndicator extends SingleValueIndicator {

	protected final CircularList values;
	private final LinearRegression linearRegression = new LinearRegression();

	public MultiValueIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
		this.values = new CircularList(length);
	}

	protected abstract boolean calculateIndicatorValue(Candle candle, double value, boolean updating);

	@Override
	protected final boolean process(Candle candle, double value, boolean updating) {
		if (updating) {
			values.update(value);
			linearRegression.update(value);
			return calculateIndicatorValue(candle, value, true);
		}

		values.add(value);
		linearRegression.add(value);
		return calculateIndicatorValue(candle, value, false);
	}

	public String toString() {
		return values.capacity() + (',' + super.toString());
	}

	public boolean movingUp(){
		return linearRegression.predict(1) > this.getValue();
	}

	public boolean movingDown(){
		return linearRegression.predict(1) < this.getValue();
	}
}
