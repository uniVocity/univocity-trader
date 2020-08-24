package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class Covariance implements Indicator {

	private long count;
	private final long interval;
	private double value;
	private final int length;

	private final CircularList l1;
	private final CircularList l2;

	private final Indicator indicator1;
	private final Indicator indicator2;

	private final Aggregator aggregator1;
	private final Aggregator aggregator2;

	public Covariance(int length, TimeInterval interval, ToDoubleFunction<Candle> indicator1, ToDoubleFunction<Candle> indicator2) {
		this(length, new FunctionIndicator(interval, indicator1), new FunctionIndicator(interval, indicator2));
	}

	public Covariance(int length, Indicator indicator1, ToDoubleFunction<Candle> indicator2) {
		this(length, indicator1, new FunctionIndicator(TimeInterval.millis(indicator1.getInterval()), indicator2));
	}

	public Covariance(int length, ToDoubleFunction<Candle> indicator1, Indicator indicator2) {
		this(length, new FunctionIndicator(TimeInterval.millis(indicator2.getInterval()), indicator1), indicator1);
	}

	public Covariance(int length, Indicator indicator1, Indicator indicator2) {
		this.l1 = new CircularList(length);
		this.l2 = new CircularList(length);
		this.indicator1 = indicator1;
		this.indicator2 = indicator2;
		this.interval = Math.min(indicator1.getInterval(), indicator2.getInterval());
		this.length = length;

		aggregator1 = getAggregator(indicator1);
		aggregator2 = getAggregator(indicator2);
	}

	private Aggregator getAggregator(Indicator indicator) {
		if (indicator instanceof AggregatedTicksIndicator) {
			return ((AggregatedTicksIndicator) indicator).getAggregator();
		}
		return null;
	}

	@Override
	public boolean accumulate(Candle candle) {
		if (indicator1.accumulate(candle) | indicator2.accumulate(candle)) {
			count++;

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

			this.value = covariance / l1.size();
			return true;
		}
		return false;
	}

	@Override
	public long getAccumulationCount() {
		return count;
	}

	@Override
	public long getInterval() {
		return interval;
	}

	@Override
	public Signal getSignal(Candle candle) {
		return Signal.NEUTRAL;
	}

	@Override
	public double getValue() {
		return value;
	}


}
