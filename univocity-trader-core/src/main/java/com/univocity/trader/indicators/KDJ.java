package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;

public class KDJ extends StochasticOscillatorD {

	public KDJ(TimeInterval interval) {
		super(interval);
	}

	public KDJ(int dLength, TimeInterval interval) {
		super(dLength, interval);
	}

	public KDJ(int dLength, int kLength, TimeInterval interval) {
		super(dLength, kLength, interval);
	}

	public double getValue() {
		double d = super.getValue();
		double k = super.k.getValue();

		return (3 * k) - (2 * d);
	}
}
