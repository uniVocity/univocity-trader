package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class AroonDown extends LowestValueIndicator {

	private double value;
	private final double length;
	private double ticksSinceLow;
	private double tmpTicksSinceLow;

	public AroonDown(TimeInterval interval) {
		this(25, interval);
	}

	public AroonDown(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public AroonDown(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length + 1, interval, valueGetter == null ? c -> c.low : valueGetter);
		this.length = length;
	}

	@Override
	protected void valueUpdated(boolean recalculated, boolean updating) {
		if (recalculated) {
			double selected = super.getValue();
			int size = this.values.size();

			for (int i = 1; i <= size; i++) {
				if (selected == values.getRecentValue(i)) {
					if (updating) {
						tmpTicksSinceLow = i - 1;
					} else {
						ticksSinceLow = i - 1;
					}
					break;
				}
			}
		} else if (updating) {
			tmpTicksSinceLow = 0;
		} else {
			ticksSinceLow = 0;
		}
	}

	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (super.calculateIndicatorValue(candle, value, updating)) {
			if (!updating) {
				this.value = ((length - ticksSinceLow) / length) * 100.0;
				ticksSinceLow++;
			} else {
				this.value = ((length - tmpTicksSinceLow) / length) * 100.0;
			}
			return true;
		}
		return false;
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
