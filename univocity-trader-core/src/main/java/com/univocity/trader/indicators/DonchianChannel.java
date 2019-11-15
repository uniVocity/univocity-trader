package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import static com.univocity.trader.indicators.Signal.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

/**
 * Lower donchian channel indicator.
 * <p>
 * Returns the lowest value of the time series within the tiemframe.
 */
public class DonchianChannel extends SingleValueCalculationIndicator {

	private HighestValueIndicator upperBand;
	private LowestValueIndicator lowerBand;

	private double upperBandValue = 0.0;
	private double lowerBandValue = 0.0;

	private int hitsDown = 0;
	private double downPrice;

	private int hitsUp = 0;
	private double upPrice;

	public DonchianChannel(int length, TimeInterval interval) {
		super(interval);
		upperBand = new HighestValueIndicator(length, millis(1), null);
		lowerBand = new LowestValueIndicator(length, millis(1), null);
	}

	@Override
	public double getValue() {
		return getMiddleBand();
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		if(updating) {
			upperBand.update(candle.high);
			lowerBand.update(candle.low);
		} else {
			upperBand.accumulate(candle.high);
			lowerBand.accumulate(candle.low);
		}
		upperBandValue = upperBand.getValue();
		lowerBandValue = lowerBand.getValue();
		return lowerBandValue + ((upperBandValue - lowerBandValue) / 2.0);
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{upperBand, lowerBand};
	}

	public double getUpperBand() {
		return upperBandValue;
	}

	public double getLowerBand() {
		return lowerBandValue;
	}

	public double getMiddleBand() {
		return super.getValue();
	}
}

