package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

public class CorrelationCoefficient extends Statistic {

	private Variance variance1;
	private Variance variance2;
	private Covariance covariance;

	public CorrelationCoefficient(int length, TimeInterval interval, ToDoubleFunction<Candle> indicator1, ToDoubleFunction<Candle> indicator2) {
		super(length, interval, indicator1, indicator2);
	}

	public CorrelationCoefficient(int length, Indicator indicator1, ToDoubleFunction<Candle> indicator2) {
		super(length, indicator1, indicator2);
	}

	public CorrelationCoefficient(int length, ToDoubleFunction<Candle> indicator1, Indicator indicator2) {
		super(length, indicator1, indicator2);
	}

	public CorrelationCoefficient(int length, Indicator indicator1, Indicator indicator2) {
		super(length, indicator1, indicator2);
	}

	@Override
	protected void initialize(Indicator indicator1, Indicator indicator2) {
		variance1 = new Variance(length, TimeInterval.millis(indicator1.getInterval()), c -> indicator1.getValue());
		variance2 = new Variance(length, TimeInterval.millis(indicator2.getInterval()), c -> indicator2.getValue());
		covariance = new Covariance(length, indicator1, indicator2);
	}

	@Override
	protected boolean indicatorsAccumulated(Candle candle) {
		return covariance.accumulate(candle) | variance1.accumulate(candle) | variance2.accumulate(candle);
	}

	@Override
	protected double calculate() {
		return covariance.getValue() / (Math.sqrt(variance1.getValue() * variance2.getValue()));
	}
}
