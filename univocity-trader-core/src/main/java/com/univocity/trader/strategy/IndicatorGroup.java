package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;

import java.util.*;

public abstract class IndicatorGroup {

	private Indicator[] indicators;

	final void initialize(Aggregator parent) {
		if (indicators != null) {
			return;
		}
		Set<Indicator> allIndicators = getAllIndicators();
		if (allIndicators == null) {
			indicators = new Indicator[0];
			return;
		}

		indicators = allIndicators.toArray(new Indicator[0]);
		for (int i = 0; i < indicators.length; i++) {
			indicators[i].initialize(parent);
		}
	}

	public final void accumulate(Candle candle) {
		for (int i = 0; i < indicators.length; i++) {
			indicators[i].accumulate(candle);
		}
		candleAccumulated(candle);
	}

	protected void candleAccumulated(Candle c) {

	}

	protected abstract Set<Indicator> getAllIndicators();
}
