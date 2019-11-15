package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import static com.univocity.trader.indicators.Signal.*;

public class AwesomeOscillator extends SingleValueCalculationIndicator {

	private final MovingAverage sma5;
	private final MovingAverage sma34;
	public final CircularList bars = new CircularList(3);

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
		sma5.update(candle);
		sma34.update(candle);
		indicator = populateIndicator(updating);
		return sma5.getValue() - sma34.getValue();
	}

	private double getMedianPrice(Candle candle) {
		return (candle.high + candle.low) / 2.0;
	}

	@Override
	public Signal calculateSignal(Candle candle) {
		return indicator;
	}

	private Signal populateIndicator(boolean updating) {
		double z, a, b, c;

		z = bars.getRecentValue(1);

		a = bars.getRecentValue(2);
		b = bars.getRecentValue(3);
		c = getValue();
		if (!updating) {
			bars.add(c);
		}
		type = "";
		if (getAccumulationCount() < 6) {
			return NEUTRAL;
		}

		//Zero line cross (upwards)
		if (((a < 0.0 || b <= 0.0) && c > 0) || (a < 0.0 && (b >= 0.0 && c > 0))) {
			type = "zero line x";
			return clearPeak(BUY);
		}
		//Zero line cross (downwards)
		if (((a > 0.0 || b >= 0.0) && c < 0) || (a > 0.0 && (b <= 0.0 && c < 0))) {
			type = "zero line x";
			return clearPeak(SELL);
		}

		//Saucer
		if (z > 0 && a > 0 && b > 0 && c > 0) { //bullish saucer
			if (a < z && b < a && c > b) {
				type = "saucer";
				return BUY;
			}
		} else if (z < 0 && a < 0 && b < 0 && c < 0) { //bearish saucer
			if (a > z && b > a && c < b) {
				type = "saucer";
				return SELL;
			}
		}

		//Twin peaks (bullish or bearish)
		if ((a > b && b < c) || (a < b && b > c)) {
			if (peak == 0.0 || trough == 0) {
				return clearPeak(NEUTRAL);
			}

			if (peak < 0) {
				if (b >= 0) {
					return clearPeak(NEUTRAL);
				}
				if (b < peak) {
					trough = 0;
				}
			} else {
				if (b <= 0) {
					return clearPeak(NEUTRAL);
				}
				if (b > peak) {
					trough = 0;
				}
			}

			if (trough > 10) {
				Signal out = NEUTRAL;
				if (peak < 0 && b < 0) { //going up
					type = "twin peaks";
					out = BUY;
				} else if (peak > 0 && b > 0) {  //going down
					type = "twin peaks";
					out = SELL;
				}
				return clearPeak(out);
			}

		}

		return NEUTRAL;
	}

	private Signal clearPeak(Signal out) {
		peak = 0.0;
		trough = 0;
		return out;
	}
}
