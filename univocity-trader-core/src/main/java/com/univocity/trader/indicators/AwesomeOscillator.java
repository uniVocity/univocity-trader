package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import static com.univocity.trader.indicators.Signal.*;

public class AwesomeOscillator extends SingleValueCalculationIndicator {

	private final MovingAverage sma5;
	private final MovingAverage sma34;

	private double veryOld;
	private double old;

	private double peak;
	private int trough;

	private Signal indicator;
	private String type;

	@Override
	protected Indicator[] children() {
		return new Indicator[]{sma5, sma34};
	}

	public AwesomeOscillator(TimeInterval interval) {
		this(5, 34, interval);
	}

	public AwesomeOscillator(int lengthShort, int lengthLong, TimeInterval interval) {
		super(interval, null);
		this.sma5 = new MovingAverage(lengthShort, interval, this::getMedianPrice);
		this.sma34 = new MovingAverage(lengthLong, interval, this::getMedianPrice);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		sma5.accumulate(candle);
		sma34.accumulate(candle);

		double next = sma5.getValue() - sma34.getValue();

		indicator = populateIndicator(updating, previousValue, next);
		return next;
	}

	private double getMedianPrice(Candle candle) {
		return (candle.high + candle.low) / 2.0;
	}

	@Override
	public String signalDescription() {
		return type;
	}

	@Override
	public Signal calculateSignal(Candle candle) {
		return indicator;
	}

	private Signal populateIndicator(boolean updating, double prev, double next) {
		double z, a, b, c;

		z = veryOld;
		a = old;
		b = prev;
		c = next;

		if (!updating) {
			veryOld = old;
			old = prev;
		}
		type = "";
		if (getAccumulationCount() < 6) {
			return NEUTRAL;
		}

		//Zero line cross (upwards)
		if (b <= 0.0 && c > 0) {
			type = "zero line cross up";
			return clearPeak(BUY, 0.0);
		}
		//Zero line cross (downwards)
		if (b >= 0.0 && c < 0) {
			type = "zero line cross down";
			return clearPeak(SELL, 0.0);
		}

		//Twin peaks (bullish or bearish)
		if(peak != 0) {
			trough++;
		} else {
			trough = 0;
		}
		if ((b > a && b > c && b > 0) || (b < a && b < c && b < 0)) {
			if(peak == 0) {
				peak = b;
			} else if(trough >= 10){
				if (peak < 0 && b < 0 && b > peak) { //going up
					type = "bullish twin peaks";
					return clearPeak(BUY, b);
				} else if (peak > 0 && b > 0 && b < peak) {  //going down
					type = "bearish twin peaks";
					return clearPeak(SELL, b);
				}
			} else {
				peak = b;
				trough = 0;
			}

		}

		//Saucer
		if (a > 0 && b > 0 && c > 0) { //bullish saucer
			if (a < z && b < a && c > b) {
				type = "bullish saucer";
				return BUY;
			}
		} else if (z < 0 && a < 0 && b < 0 && c < 0) { //bearish saucer
			if (a > z && b > a && c < b) {
				type = "bearish saucer";
				return SELL;
			}
		}


		return NEUTRAL;
	}

	private Signal clearPeak(Signal out, double peakValue) {
		peak = peakValue;
		trough = 0;
		return out;
	}
}
