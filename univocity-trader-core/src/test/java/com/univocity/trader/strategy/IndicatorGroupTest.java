package com.univocity.trader.strategy;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import org.junit.*;

import java.util.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.AverageTrueRangeTest.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;

public class IndicatorGroupTest {


	@Test
	public void testIndicatorsWorkWithAggregator() {
		TestStrategy strategy = new TestStrategy();
		strategy.ma_10_2.recalculateEveryTick(true);

		Aggregator rootAggregator = new Aggregator("parent");
		strategy.initialize(rootAggregator);

		Aggregator[] aggregators = rootAggregator.getAggregators();
		assertEquals(2, aggregators.length);

		for (int i = 0; i < prices.length; i++) {
			Candle c = newCandle(i, prices[i][2], prices[i][2], prices[i][0], prices[i][1]);

			for (Aggregator aggregator : aggregators) {
				aggregator.aggregate(c);
			}

			strategy.accumulate(c);
		}

		double avg = (/*219.4 + 240.2*/ +247.3 + 255.7 + 245.9 + 246.4 + 244.3 + 239.0 + 250.3 + 274.4 + 276.9 + 284.6 /*--this last one is partial data in the 2 minute time frame*/) / 10.0;

		assertEquals(strategy.atr_13_1.getValue(), 12.218886129739, 0.000000001);
		assertEquals(strategy.atr_5_2.getValue(), 16.87315190784, 0.000000001);
		assertEquals(strategy.ma_10_2.getValue(), avg, 0.000000001);


	}


	class TestStrategy extends IndicatorGroup {

		final AverageTrueRange atr_13_1;
		final AverageTrueRange atr_5_2;
		final MovingAverage ma_10_2;

		private Set<Indicator> indicators = new HashSet<>();

		TestStrategy() {
			indicators.add(atr_13_1 = new AverageTrueRange(13, minutes(1)));
			indicators.add(atr_5_2 = new AverageTrueRange(5, minutes(2)));
			indicators.add(ma_10_2 = new MovingAverage(10, minutes(2)));
		}

		@Override
		protected Set<Indicator> getAllIndicators() {
			return indicators;
		}
	}

}


