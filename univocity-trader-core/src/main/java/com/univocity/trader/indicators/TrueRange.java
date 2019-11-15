package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class TrueRange extends SingleValueCalculationIndicator {

	private Candle prev;

	public TrueRange(TimeInterval interval) {
		super(interval, null);
	}

	@Override
	protected double extractValue(Candle candle, boolean updating) {
		double ts = Math.abs(candle.high - candle.low);
		double ys = prev == null ? 0 : Math.abs(prev.close <= 0.0 ? 0.0 : candle.high - prev.close);
		double yst = prev == null ? 0 : Math.abs(prev.close <= 0.0 ? 0.0 : prev.close - candle.low);
		double out = Math.max(Math.max(ts, ys), yst);

		if (!updating) {
			prev = candle;
		}

		return out;
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{};
	}
}
