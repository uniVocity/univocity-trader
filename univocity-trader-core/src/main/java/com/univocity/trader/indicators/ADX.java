package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.indicators.base.adx.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ADX extends SingleValueIndicator {
	private static final double[] instants = Indicator.populateInstants(13);
	private final PlusDIIndicator plusDIIndicator;
	private final MinusDIIndicator minusDIIndicator;
	private final ModifiedMovingAverage adx;

	@Override
	protected Indicator[] children() {
		return new Indicator[]{plusDIIndicator, minusDIIndicator, adx};
	}

	public ADX(TimeInterval interval) {
		this(14, interval);
	}

	public ADX(int length, TimeInterval interval) {
		this(length, length, interval);
	}

	public ADX(int diLength, int adxLength, TimeInterval interval) {
		super(interval, null);
		adx = new ModifiedMovingAverage(adxLength, interval, null);
		plusDIIndicator = new PlusDIIndicator(diLength, interval);
		minusDIIndicator = new MinusDIIndicator(diLength, interval);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		plusDIIndicator.update(candle);
		minusDIIndicator.update(candle);

		double pdi = plusDIIndicator.getValue();
		double mdi = minusDIIndicator.getValue();


		if (pdi + mdi == 0.0) {
			value = 0.0;
		} else {
			value = (Math.abs(pdi - mdi) / (pdi + mdi)) * 100;
		}

		if (updating) {
			adx.update(value);
		} else {
			adx.accumulate(value);
		}

		return true;
	}

	@Override
	public double getValue() {
		return adx.getValue();
	}

}
