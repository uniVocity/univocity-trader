package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class AverageTrueRange extends ModifiedMovingAverage {

	private final TrueRange tr;

	public AverageTrueRange(int length, TimeInterval interval) {
		super(length, interval);
		this.tr = new TrueRange(interval);
	}

	protected double extractValue(Candle candle, boolean updating) {
		tr.update(candle);
		return tr.getValue();
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{tr};
	}
}
