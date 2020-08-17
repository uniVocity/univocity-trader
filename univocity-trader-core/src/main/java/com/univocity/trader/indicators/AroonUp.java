package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class AroonUp extends HighestValueIndicator {

	private double value;
	private final double length;
	private double ticksSinceHigh;
	private double tmpTicksSinceHigh;

	public AroonUp(TimeInterval interval) {
		this(25, interval);
	}

	public AroonUp(int length, TimeInterval interval) {
		this(length, interval, null);
	}

	public AroonUp(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length + 1, interval, valueGetter == null ? c -> c.high : valueGetter);
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
						tmpTicksSinceHigh = i - 1;
					} else {
						ticksSinceHigh = i - 1;
					}
					break;
				}
			}
		} else if (updating) {
			tmpTicksSinceHigh = 0;
		} else {
			ticksSinceHigh = 0;
		}
	}

	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (super.calculateIndicatorValue(candle, value, updating)) {
			if (!updating) {
				this.value = ((length - ticksSinceHigh) / length) * 100.0;
				ticksSinceHigh++;
			} else {
				this.value = ((length - tmpTicksSinceHigh) / length) * 100.0;
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
