package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

import static com.univocity.trader.indicators.base.AggregatedTicksIndicator.*;

public class Covariance extends Statistic {

	private CircularList l1;
	private CircularList l2;

	private Indicator indicator1;
	private Indicator indicator2;

	private Aggregator aggregator1;
	private Aggregator aggregator2;

	public Covariance(int length, TimeInterval interval, ToDoubleFunction<Candle> indicator1, ToDoubleFunction<Candle> indicator2) {
		super(length, interval, indicator1, indicator2);
	}

	public Covariance(int length, Indicator indicator1, ToDoubleFunction<Candle> indicator2) {
		super(length, indicator1, indicator2);
	}

	public Covariance(int length, ToDoubleFunction<Candle> indicator1, Indicator indicator2) {
		super(length, indicator1, indicator2);
	}

	public Covariance(int length, Indicator indicator1, Indicator indicator2) {
		super(length, indicator1, indicator2);
	}

	@Override
	protected void initialize(Indicator indicator1, Indicator indicator2) {
		this.l1 = new CircularList(length);
		this.l2 = new CircularList(length);
		this.indicator1 = indicator1;
		this.indicator2 = indicator2;
		aggregator1 = getAggregator(indicator1);
		aggregator2 = getAggregator(indicator2);
	}

	@Override
	protected boolean indicatorsAccumulated(Candle candle) {
		return indicator1.accumulate(candle) | indicator2.accumulate(candle);
	}

	@Override
	protected double calculate() {
		l1.accumulate(indicator1.getValue(), aggregator1 != null && aggregator1.getPartial() != null);
		l2.accumulate(indicator2.getValue(), aggregator2 != null && aggregator2.getPartial() != null);

		final double average1 = l1.avg();
		final double average2 = l2.avg();

		int from1 = l1.getStartingIndex();
		int from2 = l2.getStartingIndex();
		int c = Math.min(l1.size(), l2.size());

		double covariance = 0;
		while (c-- > 0) {
			covariance += (l1.get(from1) - average1) * (l2.get(from2) - average2);
			from1 = (from1 + 1) % length;
			from2 = (from2 + 1) % length;
		}

		return covariance / l1.size();
	}

}
