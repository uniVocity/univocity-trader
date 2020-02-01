package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class KAMATest {

	private static double[] values = new double[] { 110.46, 109.80, 110.17, 109.82, 110.15, 109.31, 109.05, 107.94,
			107.76, 109.24, 109.40, 108.50, 107.96, 108.55, 108.85, 110.44, 109.89, 110.70, 110.79, 110.22, 110.00,
			109.27, 106.69, 107.07, 107.92, 107.95, 107.70, 107.97, 106.09, 106.03, 107.65, 109.54, 110.26, 110.38,
			111.94, 113.59, 113.98, 113.91, 112.62, 112.20, 111.10, 110.18, 111.13, 111.55, 112.08, 111.95, 111.60,
			111.39, 112.25 };

	private static double accumulateAndGet(KAMA k, int i) {
		k.accumulate(CandleHelper.newCandle(i, values[i]));
		return k.getValue();
	}

	private static void accumulateAndTest(double expected, KAMA k, int i) {
		double value = accumulateAndGet(k, i);
		assertEquals("Unexpected value at candle " + i, expected, value, 0.0001);
	}

	@Test
	public void kama() {
		KAMA kama = new KAMA(TimeInterval.minutes(1));
		int i;

		for (i = 0; i < 9; i++) {
			accumulateAndGet(kama, i);
		}

		accumulateAndTest(109.2400, kama, i++);
		accumulateAndTest(109.2449, kama, i++);
		accumulateAndTest(109.2165, kama, i++);
		accumulateAndTest(109.1173, kama, i++);
		accumulateAndTest(109.0981, kama, i++);
		accumulateAndTest(109.0894, kama, i++);
		accumulateAndTest(109.1240, kama, i++);
		accumulateAndTest(109.1376, kama, i++);
		accumulateAndTest(109.2769, kama, i++);
		accumulateAndTest(109.4365, kama, i++);
		accumulateAndTest(109.4569, kama, i++);
		accumulateAndTest(109.4651, kama, i++);
		accumulateAndTest(109.4612, kama, i++);
		accumulateAndTest(109.3904, kama, i++);
		accumulateAndTest(109.3165, kama, i++);
		accumulateAndTest(109.2924, kama, i++);
		accumulateAndTest(109.1836, kama, i++);
		accumulateAndTest(109.0778, kama, i++);
		accumulateAndTest(108.9498, kama, i++);
		accumulateAndTest(108.4230, kama, i++);
		accumulateAndTest(108.0157, kama, i++);
		accumulateAndTest(107.9967, kama, i++);
		accumulateAndTest(108.0069, kama, i++);
		accumulateAndTest(108.2596, kama, i++);
		accumulateAndTest(108.4818, kama, i++);
		accumulateAndTest(108.9119, kama, i++);
		accumulateAndTest(109.6734, kama, i++);
		accumulateAndTest(110.4947, kama, i++);
		accumulateAndTest(111.1077, kama, i++);
		accumulateAndTest(111.4622, kama, i++);
		accumulateAndTest(111.6092, kama, i++);
		accumulateAndTest(111.5663, kama, i++);
		accumulateAndTest(111.5491, kama, i++);
		accumulateAndTest(111.5425, kama, i++);
		accumulateAndTest(111.5426, kama, i++);
		accumulateAndTest(111.5457, kama, i++);
		accumulateAndTest(111.5658, kama, i++);
		accumulateAndTest(111.5688, kama, i++);
		accumulateAndTest(111.5522, kama, i++);
		accumulateAndTest(111.5595, kama, i);
	}
}