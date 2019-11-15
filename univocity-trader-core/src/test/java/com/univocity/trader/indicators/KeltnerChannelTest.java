package com.univocity.trader.indicators;




import com.univocity.trader.candles.*;
import org.junit.*;

import java.util.function.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class KeltnerChannelTest {

	double[][] values = new double[][]{
			{11577.43, 11670.75, 11711.47, 11577.35},
			{11670.90, 11691.18, 11698.22, 11635.74},
			{11688.61, 11722.89, 11742.68, 11652.89},
			{11716.93, 11697.31, 11736.74, 11667.46},
			{11696.86, 11674.76, 11726.94, 11599.68},
			{11672.34, 11637.45, 11677.33, 11573.87},
			{11638.51, 11671.88, 11704.12, 11635.48},
			{11673.62, 11755.44, 11782.23, 11673.62},
			{11753.70, 11731.90, 11757.25, 11700.53},
			{11732.13, 11787.38, 11794.15, 11698.83},
			{11783.82, 11837.93, 11858.78, 11777.99},
			{11834.21, 11825.29, 11861.24, 11798.46},
			{11823.70, 11822.80, 11845.16, 11744.77},
			{11822.95, 11871.84, 11905.48, 11822.80},
			{11873.43, 11980.52, 11982.94, 11867.98},
			{11980.52, 11977.19, 11985.97, 11898.74},
			{11978.85, 11985.44, 12020.52, 11961.83},
			{11985.36, 11989.83, 12019.53, 11971.93},
			{11824.39, 11891.93, 11891.93, 11817.88},
			{11892.50, 12040.16, 12050.75, 11892.50},
			{12038.27, 12041.97, 12057.91, 12018.51},
			{12040.68, 12062.26, 12080.54, 11981.05},
			{12061.73, 12092.15, 12092.42, 12025.78},
			{12092.38, 12161.63, 12188.76, 12092.30},
			{12152.70, 12233.15, 12238.79, 12150.05},
			{12229.29, 12239.89, 12254.23, 12188.19},
			{12239.66, 12229.29, 12239.66, 12156.94},
			{12227.78, 12273.26, 12285.94, 12180.48},
			{12266.83, 12268.19, 12276.21, 12235.91},
			{12266.75, 12226.64, 12267.66, 12193.27},
			{12219.79, 12288.17, 12303.16, 12219.79},
			{12287.72, 12318.14, 12331.31, 12253.24},
			{12389.74, 12212.79, 12389.82, 12176.31},
	};

	private Candle newCandle(int i) {
		return CandleHelper.newCandle(i, values[i][0], values[i][1], values[i][2], values[i][3]);
	}

	private double accumulateAndReturn(KeltnerChannel indicator, int i, ToDoubleFunction<KeltnerChannel> f) {
		Candle c = newCandle(i);
		indicator.accumulate(c);
		return f.applyAsDouble(indicator);
	}

	@Test
	public void keltnerChannelMiddleIndicatorTest() {
		KeltnerChannel km = new KeltnerChannel(14, minutes(1), c->c.close);

		for (int i = 0; i < 13; i++) {
			accumulateAndReturn(km, i, KeltnerChannel::getMiddleBand);
		}

		assertEquals(11764.23, accumulateAndReturn(km, 13, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11793.0687, accumulateAndReturn(km, 14, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11817.6182, accumulateAndReturn(km, 15, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11839.9944, accumulateAndReturn(km, 16, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11859.9725, accumulateAndReturn(km, 17, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11864.2335, accumulateAndReturn(km, 18, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11887.6903, accumulateAndReturn(km, 19, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11908.2609, accumulateAndReturn(km, 20, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11928.7941, accumulateAndReturn(km, 21, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11950.5749, accumulateAndReturn(km, 22, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11978.7156, accumulateAndReturn(km, 23, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12012.6402, accumulateAndReturn(km, 24, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12042.9401, accumulateAndReturn(km, 25, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12067.7868, accumulateAndReturn(km, 26, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12095.1832, accumulateAndReturn(km, 27, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12118.2508, accumulateAndReturn(km, 28, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12132.7027, accumulateAndReturn(km, 29, KeltnerChannel::getMiddleBand), 0.001);
	}

	@Test
	public void keltnerChannelLowerIndicatorTest() {
		KeltnerChannel km = new KeltnerChannel(14, 14, minutes(1), c->c.close);

		for (int i = 0; i < 13; i++) {
			accumulateAndReturn(km, i, KeltnerChannel::getLowerBand);
		}

		assertEquals(11556.5468, accumulateAndReturn(km, 13, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11583.7971, accumulateAndReturn(km, 14, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11610.8331, accumulateAndReturn(km, 15, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11639.5955, accumulateAndReturn(km, 16, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11667.0877, accumulateAndReturn(km, 17, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11660.5619, accumulateAndReturn(km, 18, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11675.8782, accumulateAndReturn(km, 19, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11705.9497, accumulateAndReturn(km, 20, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11726.7208, accumulateAndReturn(km, 21, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11753.4154, accumulateAndReturn(km, 22, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11781.8375, accumulateAndReturn(km, 23, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11817.1476, accumulateAndReturn(km, 24, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11851.9771, accumulateAndReturn(km, 25, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11878.6139, accumulateAndReturn(km, 26, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11904.4570, accumulateAndReturn(km, 27, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11935.3907, accumulateAndReturn(km, 28, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11952.2012, accumulateAndReturn(km, 29, KeltnerChannel::getLowerBand), 0.001);
	}

	@Test
	public void keltnerChannelUpperIndicatorTest() {
		KeltnerChannel km = new KeltnerChannel(14, 14, minutes(1), c->c.close);

		for (int i = 0; i < 13; i++) {
			accumulateAndReturn(km, i, KeltnerChannel::getUpperBand);
		}

		assertEquals(11971.9132, accumulateAndReturn(km, 13, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12002.3402, accumulateAndReturn(km, 14, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12024.4032, accumulateAndReturn(km, 15, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12040.3933, accumulateAndReturn(km, 16, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12052.8572, accumulateAndReturn(km, 17, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12067.9050, accumulateAndReturn(km, 18, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12099.5025, accumulateAndReturn(km, 19, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12110.5722, accumulateAndReturn(km, 20, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12130.8675, accumulateAndReturn(km, 21, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12147.7344, accumulateAndReturn(km, 22, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12175.5937, accumulateAndReturn(km, 23, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12208.1327, accumulateAndReturn(km, 24, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12233.9032, accumulateAndReturn(km, 25, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12256.9596, accumulateAndReturn(km, 26, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12285.9094, accumulateAndReturn(km, 27, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12301.1108, accumulateAndReturn(km, 28, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12313.2042, accumulateAndReturn(km, 29, KeltnerChannel::getUpperBand), 0.001);
	}

	private Candle newCandle1(int i) {
		double f = i % 2 == 0 ? 1.01 : 0.99;
		return CandleHelper.newCandle(i, values[i][0], values[i][1] * f, values[i][2] * 0.99, values[i][3] * 1.01);
	}

	private Candle newCandle2(int i) {
		return CandleHelper.newCandle(i + 1, values[i][0] * 0.99, values[i][1], values[i][2], values[i][3]);
	}

	private double accumulateAndReturn2(KeltnerChannel indicator, int i, ToDoubleFunction<KeltnerChannel> f) {
		indicator.accumulate(newCandle1(i));
		indicator.accumulate(newCandle2(i));
		return f.applyAsDouble(indicator);
	}

	@Test
	public void keltnerChannelMiddleIndicatorTest2() {
		KeltnerChannel km = new KeltnerChannel(14, minutes(2), c->c.close);

		for (int i = 0; i < 13; i++) {
			accumulateAndReturn2(km, i, KeltnerChannel::getMiddleBand);
		}

		assertEquals(11764.23, accumulateAndReturn2(km, 13, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11793.0687, accumulateAndReturn2(km, 14, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11817.6182, accumulateAndReturn2(km, 15, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11839.9944, accumulateAndReturn2(km, 16, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11859.9725, accumulateAndReturn2(km, 17, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11864.2335, accumulateAndReturn2(km, 18, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11887.6903, accumulateAndReturn2(km, 19, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11908.2609, accumulateAndReturn2(km, 20, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11928.7941, accumulateAndReturn2(km, 21, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11950.5749, accumulateAndReturn2(km, 22, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(11978.7156, accumulateAndReturn2(km, 23, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12012.6402, accumulateAndReturn2(km, 24, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12042.9401, accumulateAndReturn2(km, 25, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12067.7868, accumulateAndReturn2(km, 26, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12095.1832, accumulateAndReturn2(km, 27, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12118.2508, accumulateAndReturn2(km, 28, KeltnerChannel::getMiddleBand), 0.001);
		assertEquals(12132.7027, accumulateAndReturn2(km, 29, KeltnerChannel::getMiddleBand), 0.001);
	}

	@Test
	public void keltnerChannelLowerIndicatorTest2() {
		KeltnerChannel km = new KeltnerChannel(14, 14, minutes(2), c->c.close);

		for (int i = 0; i < 13; i++) {
			accumulateAndReturn2(km, i, KeltnerChannel::getLowerBand);
		}

		assertEquals(11556.5468, accumulateAndReturn2(km, 13, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11583.7971, accumulateAndReturn2(km, 14, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11610.8331, accumulateAndReturn2(km, 15, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11639.5955, accumulateAndReturn2(km, 16, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11667.0877, accumulateAndReturn2(km, 17, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11660.5619, accumulateAndReturn2(km, 18, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11675.8782, accumulateAndReturn2(km, 19, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11705.9497, accumulateAndReturn2(km, 20, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11726.7208, accumulateAndReturn2(km, 21, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11753.4154, accumulateAndReturn2(km, 22, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11781.8375, accumulateAndReturn2(km, 23, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11817.1476, accumulateAndReturn2(km, 24, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11851.9771, accumulateAndReturn2(km, 25, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11878.6139, accumulateAndReturn2(km, 26, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11904.4570, accumulateAndReturn2(km, 27, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11935.3907, accumulateAndReturn2(km, 28, KeltnerChannel::getLowerBand), 0.001);
		assertEquals(11952.2012, accumulateAndReturn2(km, 29, KeltnerChannel::getLowerBand), 0.001);
	}

	@Test
	public void keltnerChannelUpperIndicatorTest2() {
		KeltnerChannel km = new KeltnerChannel(14, 14, minutes(2), c->c.close);

		for (int i = 0; i < 13; i++) {
			accumulateAndReturn2(km, i, KeltnerChannel::getUpperBand);
		}

		assertEquals(11971.9132, accumulateAndReturn2(km, 13, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12002.3402, accumulateAndReturn2(km, 14, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12024.4032, accumulateAndReturn2(km, 15, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12040.3933, accumulateAndReturn2(km, 16, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12052.8572, accumulateAndReturn2(km, 17, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12067.9050, accumulateAndReturn2(km, 18, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12099.5025, accumulateAndReturn2(km, 19, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12110.5722, accumulateAndReturn2(km, 20, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12130.8675, accumulateAndReturn2(km, 21, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12147.7344, accumulateAndReturn2(km, 22, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12175.5937, accumulateAndReturn2(km, 23, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12208.1327, accumulateAndReturn2(km, 24, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12233.9032, accumulateAndReturn2(km, 25, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12256.9596, accumulateAndReturn2(km, 26, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12285.9094, accumulateAndReturn2(km, 27, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12301.1108, accumulateAndReturn2(km, 28, KeltnerChannel::getUpperBand), 0.001);
		assertEquals(12313.2042, accumulateAndReturn2(km, 29, KeltnerChannel::getUpperBand), 0.001);
	}
}
