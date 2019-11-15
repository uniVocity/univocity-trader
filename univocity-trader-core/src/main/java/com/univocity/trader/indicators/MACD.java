package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

import static com.univocity.trader.indicators.Signal.*;


/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MACD extends SingleValueIndicator {

	private final SingleValueIndicator emaShort;
	private final SingleValueIndicator emaLong;

	private final SingleValueIndicator signalLine; //from macdLine

	private String params;

	@Override
	protected Indicator[] children() {
		return new Indicator[]{emaShort, emaLong, signalLine};
	}

	public MACD(TimeInterval interval) {
		this(12, 26, 9, interval);
	}

	public MACD(int shortCount, int longCount, int macdCount, TimeInterval interval) {
		this(shortCount, longCount, macdCount, interval, c -> c.close);
	}

	public MACD(int shortCount, int longCount, int macdCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, null);
		emaShort = getShortAverageIndicator(shortCount, interval, valueGetter);
		emaLong = getLongAverageIndicator(longCount, interval, valueGetter);
		signalLine = getSignalAverageIndicator(macdCount, interval);
		this.params = shortCount + "," + longCount + "," + macdCount + ",";
	}

	protected SingleValueIndicator getAverageIndicator(int count, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return new ExponentialMovingAverage(count, interval, valueGetter);
	}

	protected SingleValueIndicator getShortAverageIndicator(int shortCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return getAverageIndicator(shortCount, interval, valueGetter);
	}

	protected SingleValueIndicator getLongAverageIndicator(int longCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		return getAverageIndicator(longCount, interval, valueGetter);
	}

	protected SingleValueIndicator getSignalAverageIndicator(int macdCount, TimeInterval interval) {
		return getAverageIndicator(macdCount, interval, null);
	}

	public double getHistogram() {
		return getMacdLine() - signalLine.getValue();
	}

	public double getMacdSignal() {
		return this.signalLine.getValue();
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		emaShort.update(candle);
		emaLong.update(candle);

		final double macdValue = emaShort.getValue() - emaLong.getValue();
		if (updating) {
			signalLine.update(macdValue);
		} else {
			signalLine.accumulate(macdValue);
		}
		return true;
	}

	public double getMacdLine() {
		return emaShort.getValue() - emaLong.getValue();
	}

	@Override
	public double getValue() {
		return getMacdLine();
	}

	public String toString() {
		return params + super.toString();
	}
}
