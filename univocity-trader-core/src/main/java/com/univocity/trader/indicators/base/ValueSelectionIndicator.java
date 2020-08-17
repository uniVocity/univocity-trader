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

	private void recalculate(boolean updating) {
		updateValue = selectedValue = initialValue();
		final int len = values.size();
		int tmp = len;
		int start = values.i - 1;
		while (--tmp >= 0) {
			if (start < 0) {
				start = len - 1;
			}
			double v = values.get(start--);
			updateSelection(v, tmp);
		}
		valueUpdated(true, updating);
	}

	private boolean updateSelection(double value, int ticksLeft) {
		double selected = select(value, selectedValue);
		if (selected != selectedValue) {
			selectedValue = value;
			this.ticksLeft = ticksLeft;
			return true;
		}
		return false;
	}

	protected void valueUpdated(boolean recalculated, boolean updating) {

	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (updating) {
			if (ticksLeft <= 0) {
				recalculate(true);
			}
			double selected = select(value, selectedValue);
			updateValue = selected == value ? selected : initialValue();
		} else {
			updateValue = initialValue();

			ticksLeft--;
			if (ticksLeft <= 0) {
				recalculate(false);
			} else {
				if(updateSelection(value, values.size() - 1)) {
					valueUpdated(false, false);
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
