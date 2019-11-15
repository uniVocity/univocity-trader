package com.univocity.trader.candles;


import com.univocity.trader.strategy.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public class CandleHelper {

	public static Candle newCandle() {
		return newCandle(0, 0.0);
	}


	public static Candle newCandle(double amount) {
		return newCandle(0, amount);
	}

	public static Candle newCandle(int i, double amount) {
		return newCandle(i, amount, amount, amount, amount);
	}

	public static Candle newCandle(int i, double open, double close, double high, double low) {
		return newCandle(i, open, close, high, low, 1.0);
	}

	public static Candle newCandle(int i, double open, double close, double high, double low, double volume) {
		return new Candle(MINUTE.ms * (i), MINUTE.ms * (i + 1) - 1, open, high, low, close, volume);
	}

	public static Candle newTick(long openTime, long closeTime, double amount) {
		return newTick(openTime, closeTime, amount, amount, amount, amount);
	}

	public static Candle newTick(long openTime, long closeTime, double open, double close, double high, double low) {
		return newTick(openTime, closeTime, open, close, high, low, 0.0);
	}

	public static Candle newTick(long openTime, long closeTime, double open, double close, double high, double low, double volume) {
		return new Candle(openTime, closeTime, open, high, low, close, volume);
	}

	public static double updateTick(Indicator indicator, long openTime, long closeTime, double value) {
		indicator.update(newTick(openTime, closeTime, value));
		return indicator.getValue();
	}

	public static double update(Indicator indicator, int minute, double value) {
		indicator.update(newCandle(minute, value));
		return indicator.getValue();
	}

	public static double accumulateTick(Indicator indicator, long openTime, long closeTime, double value) {
		return accumulate(indicator, newTick(openTime, closeTime, value));
	}

	public static double accumulate(Indicator indicator, int minute, double value) {
		return accumulate(indicator, newCandle(minute, value));
	}

	public static double accumulate(Indicator indicator, Candle candle) {
		indicator.accumulate(candle);
		return indicator.getValue();
	}
}
