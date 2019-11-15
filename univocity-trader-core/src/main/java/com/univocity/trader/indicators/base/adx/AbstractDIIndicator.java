package com.univocity.trader.indicators.base.adx;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class AbstractDIIndicator extends SingleValueIndicator {

	private final AverageTrueRange atr;
	private final AbstractDMIndicator dm;
	private final ModifiedMovingAverage avg;

	private double value;

	public AbstractDIIndicator(int length, TimeInterval interval) {
		super(interval, null);
		this.avg = new ModifiedMovingAverage(length, interval);
		this.atr = new AverageTrueRange(length, interval);
		this.dm = getDMIndicator(interval);
	}

	protected abstract AbstractDMIndicator getDMIndicator(TimeInterval interval);

	@Override
	protected final boolean process(Candle candle, double v, boolean updating) {
		dm.update(candle);
		atr.update(candle);
		if (updating) {
			avg.update(dm.getValue());
		} else {
			avg.accumulate(dm.getValue());
		}

		double atr = this.atr.getValue();
		if (atr == 0.0) {
			this.value = 0.0;
		} else {
			this.value = (avg.getValue() / atr) * 100.0;
		}
		return true;
	}

	public final double getValue() {
		return value;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{atr, dm, avg};
	}
}
