package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class BollingerBand extends MovingAverage {

	private double stddev;
	private final double multiplier;

	public BollingerBand(TimeInterval interval) {
		this(12, 2.0, interval);
	}

	public BollingerBand(int length, TimeInterval interval) {
		this(length, 2.0, interval);
	}

	public BollingerBand(int length, double multiplier, TimeInterval interval) {
		this(length, multiplier, interval, null);
	}

	public BollingerBand(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		this(length, 2.0, interval, valueGetter);
	}

	public BollingerBand(int length, double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(length, interval, valueGetter == null ? c -> c.close : valueGetter);
		this.multiplier = multiplier;
	}

	private double getStandardDeviation() {
		return stddev;
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		boolean out = super.calculateIndicatorValue(candle, value, updating);
		if (out) {
			updateStandardDeviation();
		}
		return out;
	}


	private void updateStandardDeviation() {
		double avg = getMiddleBand();
		stddev = 0.0;
		for (int i = 0; i < values.size(); i++) {
			stddev += Math.pow(values.get(i) - avg, 2.0);
		}
		stddev = stddev / (double) values.capacity();
		stddev = Math.sqrt(stddev);
	}

	public double getUpperBand() {
		double deviation = getStandardDeviation();
		double middle = getMiddleBand();
		return middle + (multiplier * deviation);
	}

	public double getLowerBand() {
		double deviation = getStandardDeviation();
		double middle = getMiddleBand();
		return middle - (multiplier * deviation);
	}

	public double getMiddleBand() {
		return super.getValue();
	}

}
