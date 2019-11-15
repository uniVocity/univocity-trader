package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import static com.univocity.trader.indicators.Signal.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class BollingerBand extends MovingAverage {

	private double stddev;

	public BollingerBand(TimeInterval interval) {
		this(12, interval);
	}

	public BollingerBand(int length, TimeInterval interval) {
		super(length, interval);
	}

	private double getStandardDeviation() {
		return stddev;
	}

	@Override
	protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
		boolean out = super.calculateIndicatorValue(candle,value, updating);
		if(out){
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
		return middle + (2.0 * deviation);
	}

	public double getLowerBand() {
		double deviation = getStandardDeviation();
		double middle = getMiddleBand();
		return middle - (2.0 * deviation);
	}

	public double getMiddleBand() {
		return super.getValue();
	}

}
