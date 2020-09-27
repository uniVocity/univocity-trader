package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

import static com.univocity.trader.indicators.Signal.*;

public class ThreeBlackCrows extends MovingAverage {

	private final CircularList averageLowerShadowList;
	private final double factor;

	private Candle whiteCandle;
	private Candle c1;
	private Candle c2;

	public ThreeBlackCrows(TimeInterval interval) {
		this(3, 0.3, interval);
	}

	public ThreeBlackCrows(double factor, TimeInterval interval) {
		this(3, factor, interval);
	}

	public ThreeBlackCrows(int length, TimeInterval interval) {
		this(length, 0.3, interval);
	}

	public ThreeBlackCrows(int length, double factor, TimeInterval interval) {
		super(length, interval, c -> c.close > c.open ? c.open - c.low : c.close - c.low);
		this.averageLowerShadowList = new CircularList(4);
		this.factor = factor;
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		if (super.calculateIndicatorValue(candle, value, updating)) {
			averageLowerShadowList.add(super.getValue());
			return true;
		}
		return false;
	}

	private boolean hasVeryShortLowerShadow(Candle candle) {
		double currentLowerShadow = valueGetter.applyAsDouble(candle);
		double averageLowerShadow = averageLowerShadowList.first();
		return currentLowerShadow < (averageLowerShadow * factor);
	}

	private boolean isDeclining(Candle candle, Candle prev) {
		return candle.open < prev.open && candle.open > prev.close && candle.close < prev.close;
	}

	private boolean isBlackCrow(Candle candle, Candle prev) {
		if (candle.isRed()) {
			if (prev.isGreen()) {
				return hasVeryShortLowerShadow(candle) && candle.open < prev.high;
			} else {
				return hasVeryShortLowerShadow(candle) && isDeclining(candle, prev);
			}
		}
		return false;
	}

	@Override
	public double getValue() {
		return getSignal(null) == SELL ? 1.0 : 0.0;
	}

	@Override
	protected Signal calculateSignal(Candle candle) {
		Signal signal;
		if (whiteCandle == null) {
			signal = Signal.NEUTRAL;
		} else {
			signal = whiteCandle.isGreen() && isBlackCrow(c1, whiteCandle) && isBlackCrow(c2, c1) && isBlackCrow(candle, c2) ? SELL : Signal.NEUTRAL;
		}
		whiteCandle = c1;
		c1 = c2;
		c2 = candle;
		return signal;
	}

	@Override
	public String signalDescription() {
		return getSignal(null) == SELL ? "3 black crows" : "";
	}
}
