package com.univocity.trader.indicators.base;

import com.univocity.trader.candles.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class ValueSelectionIndicator extends MultiValueIndicator {

	private double updateValue = initialValue();
	private double selectedValue = initialValue();
	private int ticksLeft;

	public ValueSelectionIndicator(int length, TimeInterval interval) {
		this(length, interval, c -> c.close);
	}

	public ValueSelectionIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter);
	}

	private void recalculate() {
		updateValue = selectedValue = initialValue();
		final int len = values.size();
		int tmp = len;
		int start = values.i - 1;
		while (--tmp >= 0) {
			if (start < 0) {
				start = len - 1;
			}
			double v = values.get(start--);

			double selected = select(v, selectedValue);
			if (selected == v) {
				selectedValue = v;
				ticksLeft = tmp;
			}
		}
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (updating) {
			if (ticksLeft <= 0) {
				recalculate();
			}
			double selected = select(value, selectedValue);
			updateValue = selected == value ? selected : initialValue();
		} else {
			updateValue = initialValue();

			ticksLeft--;
			if (ticksLeft <= 0) {
				recalculate();
			} else {
				double selected = select(value, selectedValue);
				if (selected == value) {
					selectedValue = value;
					ticksLeft = values.size() - 1;
				}
			}
		}

		return true;
	}

	protected abstract double select(double v1, double v2);

	protected abstract double initialValue();

	@Override
	public double getValue() {
		return select(selectedValue, updateValue);
	}

}
