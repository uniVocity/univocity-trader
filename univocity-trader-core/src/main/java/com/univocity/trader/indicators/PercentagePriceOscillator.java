package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class PercentagePriceOscillator extends SingleValueIndicator {

	private double value;

	private final ExponentialMovingAverage shortTermEma;
	private final ExponentialMovingAverage longTermEma;
	private final ExponentialMovingAverage signal;

	public PercentagePriceOscillator(TimeInterval interval) {
		this(12, 26, 9, interval, c -> c.close);
	}

	public PercentagePriceOscillator(int shortBarCount, int longBarCount, int signalBarCount, TimeInterval interval) {
		this(shortBarCount, longBarCount, signalBarCount, interval, c -> c.close);
	}

	public PercentagePriceOscillator(int shortBarCount, int longBarCount, int signalBarCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		valueGetter = valueGetter == null ? c -> c.close : valueGetter;
		this.shortTermEma = new ExponentialMovingAverage(shortBarCount, interval, valueGetter);
		this.longTermEma = new ExponentialMovingAverage(longBarCount, interval, valueGetter);
		this.signal = new ExponentialMovingAverage(signalBarCount, interval);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (shortTermEma.accumulate(candle)) {
			double shortEmaValue = shortTermEma.getValue();

			longTermEma.accumulate(candle);
			double longEmaValue = longTermEma.getValue();

			this.value = ((shortEmaValue - longEmaValue) / longEmaValue) * 100.0;

			this.signal.accumulate(this.value);
			return true;
		}
		return false;
	}

	@Override
	public double getValue() {
		return value;
	}

	public double getSignal() {
		return signal.getValue();
	}

	public double getHistogram() {
		return value - getSignal();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{shortTermEma, longTermEma, signal};
	}

}
