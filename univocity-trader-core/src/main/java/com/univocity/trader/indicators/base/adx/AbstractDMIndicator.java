package com.univocity.trader.indicators.base.adx;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

public abstract class AbstractDMIndicator extends SingleValueCalculationIndicator {

	private Candle prev;

	public AbstractDMIndicator(TimeInterval interval) {
		super(interval, null);
	}

	@Override
	protected final double calculate(Candle c, double value, double previousValue, boolean updating) {
		if (prev == null) {
			prev = updating ? null : c;
			return 0.0;
		} else {
			double upMove = c.high - prev.high;
			double downMove = prev.low - c.low;
			prev = updating ? null : c;
			return calculate(upMove, downMove);
		}
	}

	protected abstract double calculate(double upMove, double downMove);


}