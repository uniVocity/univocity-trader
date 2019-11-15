package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

import java.math.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface Indicator {

	long getAccumulationCount();

	double getValue();

	boolean accumulate(Candle candle);

	boolean update(Candle candle);

	long getInterval();

	Signal getSignal(Candle candle);

	static double[] populateInstants(int length){
		double[] instants = new double[length];
		for (int i = 0; i < length; i++) {
			instants[i] = i;
		}
		return instants;
	}

	default void initialize(Aggregator aggregator) {
		throw new IllegalStateException("method initialize(aggregator, symbol) must be implemented in " + this.getClass().getSimpleName());
	}

	default void recalculateEveryTick(boolean recalculateEveryTick) {

	}
}
