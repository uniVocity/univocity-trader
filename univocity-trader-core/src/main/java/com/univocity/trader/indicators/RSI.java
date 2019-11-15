package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class RSI extends SingleValueIndicator {

	private final ModifiedMovingAverage averageGainIndicator;
	private final ModifiedMovingAverage averageLossIndicator;

	private Candle prev;

	private double value;

	public RSI(TimeInterval interval) {
		this(14, interval);
	}

	public RSI(int length, TimeInterval interval) {
		super(interval, null);
		this.averageGainIndicator = new ModifiedMovingAverage(length, interval, this::calculateGain);
		this.averageLossIndicator = new ModifiedMovingAverage(length, interval, this::calculateLoss);
	}


	private double calculateGain(Candle c) {
		double current = c.close;
		double previous = prev.close;

		if (current > previous) {
			return current - previous;
		}
		return 0.0;
	}

	private double calculateLoss(Candle c) {
		double current = c.close;
		double previous = prev.close;

		if (current < previous) {
			return previous - current;
		}
		return 0.0;
	}


	@Override
	protected boolean process(Candle candle, double v, boolean updating) {
		if (prev == null) {
			prev = candle;
			averageGainIndicator.accumulate(0.0);
			averageLossIndicator.accumulate(0.0);
			return false;
		}

		averageGainIndicator.update(candle);
		averageLossIndicator.update(candle);

		double averageGain = averageGainIndicator.getValue();
		double averageLoss = averageLossIndicator.getValue();
		if (averageLoss == 0.0) {
			if (averageGain == 0.0) {
				this.value = 0.0;
			} else {
				this.value = 100.0;
			}
		} else {
			double relativeStrength = averageGain / averageLoss;
			this.value = 100.0 - (100.0 / (1.0 + relativeStrength));
		}

		if(!updating) {
			prev = candle;
		}
		return true;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{averageLossIndicator, averageGainIndicator};
	}
}
