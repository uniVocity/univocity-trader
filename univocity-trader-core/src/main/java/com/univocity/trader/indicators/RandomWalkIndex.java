package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

public class RandomWalkIndex extends TrueRange {

	private double rwiLow;
	private double rwiHigh;

	private final int length;

	private final CircularList highs;
	private final CircularList lows;
	private final CircularList trHistory;

	public RandomWalkIndex(TimeInterval interval) {
		this(8, interval);
	}

	public RandomWalkIndex(int length, TimeInterval interval) {
		super(interval);
		this.highs = new CircularList(length);
		this.lows = new CircularList(length);
		this.trHistory = new CircularList(length);
		this.length = length;
	}

	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		double tr = super.calculate(candle, value, previousValue, updating);

		trHistory.accumulate(tr, updating);
		highs.accumulate(candle.high, updating);
		lows.accumulate(candle.low, updating);

		rwiLow = 0;
		rwiHigh = 0;

		int len = Math.min(length, lows.size());

		for (int n = 2; n < len; n++) {
			double atrN = 0.0;

//			this uses Modified Moving Average to calculate the ATR
//			final double multiplier = 1.0 / n;
//			int p = n;
//			do {
//				if(p == n){
//					atrN = trHistory.getRecentValue(p);
//				} else {
//					atrN = ((trHistory.getRecentValue(p) - atrN) * multiplier) + atrN;
//				}
//			} while (--p > 0);

//			using simple moving average calculation
			int p = n;
			do {
				atrN += trHistory.getRecentValue(p);
			} while (--p > 0);
			atrN = atrN / n;

			double sqrtN = Math.sqrt(n);
			double divisor = (atrN * sqrtN);
			double highN = highs.getRecentValue(n);
			double lowN = lows.getRecentValue(n);
			rwiLow = Math.max(rwiLow, (highN - candle.low) / divisor);
			rwiHigh = Math.max(rwiHigh, (candle.high - lowN) / divisor);
		}

		return tr;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}

	public double getRwiLow() {
		return rwiLow;
	}

	public double getRwiHigh() {
		return rwiHigh;
	}

	public double getOscillator(){
		return (rwiHigh - rwiLow) / (rwiHigh + rwiLow);
	}

}
