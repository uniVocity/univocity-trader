package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

public class PercentRankIndicator extends SingleValueIndicator {

	private CircularList values;
	private RateOfChange roc;
	private double value;

	public PercentRankIndicator(TimeInterval interval) {
		this(100, interval);
	}

	public PercentRankIndicator(int length, TimeInterval interval) {
		super(interval, null);
		this.values = new CircularList(length);
		this.roc = new RateOfChange(1, interval);
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (roc.update(candle)) {
			double change = roc.getValue();

			if (updating) {
				values.update(change);
			} else {
				values.add(change);
			}

			int size = values.size();
			double count = 0.0;
			for (int i = 0; i < size; i++) {
				if (values.values[i] < change) {
					count++;
				}
			}
			this.value = (count / size) * 100.0;
			return true;
		}

		return false;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{roc};
	}
}
