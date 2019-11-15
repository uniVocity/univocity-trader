package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class CHOP extends SingleValueCalculationIndicator {

	public static final double HIGH_CHOPPINESS_VALUE = 61.8;
	public static final double LOW_CHOPPINESS_VALUE = 38.2;

	private final CircularList atrIndicators;
	private final AverageTrueRange atrIndicator;
	private final double log10n;
	private final HighestValueIndicator hvi;
	private final LowestValueIndicator lvi;
	private final double scaleUpTo;

	@Override
	protected Indicator[] children() {
		return new Indicator[]{atrIndicator, hvi, lvi};
	}

	public CHOP(TimeInterval interval) {
		this(14, 100, interval);
	}

	public CHOP(int length, int scaleTo, TimeInterval interval) { //@param scaleTo maximum value to scale this oscillator, usually '1' or '100'
		super(interval);
		this.atrIndicator = new AverageTrueRange(1, interval); // ATR(1) = Average True Range (Period of 1)
		this.atrIndicators = new CircularList(length);
		hvi = new HighestValueIndicator(length, interval, c -> c.high);
		lvi = new LowestValueIndicator(length, interval, c -> c.low);
		this.log10n = Math.log10(length);
		this.scaleUpTo = scaleTo;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		hvi.update(candle);
		lvi.update(candle);
		atrIndicator.update(candle);

		if (updating) {
			atrIndicators.update(this.atrIndicator.getValue());
		} else {
			atrIndicators.add(this.atrIndicator.getValue());
		}

		if(getAccumulationCount() == 0){
			return 0.0;
		}

		return scaleUpTo * (Math.log10(atrIndicators.sum() / (hvi.getValue() - lvi.getValue()))) / log10n;
	}

	public boolean isMarketTrending(double lowChopinessValue) {
		return getValue() < lowChopinessValue;
	}

	public boolean isMarketTrending() {
		return isMarketTrending(LOW_CHOPPINESS_VALUE);
	}

	public boolean isMarketSideways() {
		return isMarketSideways(HIGH_CHOPPINESS_VALUE);
	}

	public boolean isMarketSideways(double highChopinessValue) {
		return getValue() > highChopinessValue;
	}
}
