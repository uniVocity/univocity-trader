package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

public class Doji extends RealBodyIndicator {

	private final double bodyFactor;
	private double value;
	private double prevAverageBodyHeightInd;

	private final CircularList averageBodyHeightInd;

	public Doji(TimeInterval interval) {
		this(0.03, 10, interval);
	}

	public Doji(double bodyFactor, int length, TimeInterval interval) {
		super(interval);
		this.averageBodyHeightInd = new CircularList(length);
		this.bodyFactor = bodyFactor;
		this.prevAverageBodyHeightInd = 0;
	}


	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (super.process(candle, value, updating)) {
			this.averageBodyHeightInd.accumulate(Math.abs(super.getValue()), updating);

			if (getAccumulationCount() == 0) {
				this.value = Math.abs(super.getValue()) == 0 ? 1 : 0;
				return true;
			}

			double averageBodyHeight = prevAverageBodyHeightInd;
			double currentBodyHeight = Math.abs(super.getValue());

			this.value = currentBodyHeight < (averageBodyHeight * this.bodyFactor) ? 1 : 0;
			this.prevAverageBodyHeightInd = averageBodyHeightInd.avg();

			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return this.value;
	}
}
