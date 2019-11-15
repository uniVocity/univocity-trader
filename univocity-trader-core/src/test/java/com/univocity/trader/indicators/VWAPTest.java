package com.univocity.trader.indicators;




import com.univocity.trader.candles.*;
import com.univocity.trader.strategy.*;
import org.junit.*;


import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;

public class VWAPTest {


	double[][] values = new double[][]{
			{44.98, 45.05, 45.17, 44.96, 1},
			{45.05, 45.10, 45.15, 44.99, 2},
			{45.11, 45.19, 45.32, 45.11, 1},
			{45.19, 45.14, 45.25, 45.04, 3},
			{45.12, 45.15, 45.20, 45.10, 1},
			{45.15, 45.14, 45.20, 45.10, 2},
			{45.13, 45.10, 45.16, 45.07, 1},
			{45.12, 45.15, 45.22, 45.10, 5},
			{45.15, 45.22, 45.27, 45.14, 1},
			{45.24, 45.43, 45.45, 45.20, 1},
			{45.43, 45.44, 45.50, 45.39, 1},
			{45.43, 45.55, 45.60, 45.35, 5},
			{45.58, 45.55, 45.61, 45.39, 7},
			{45.45, 45.01, 45.55, 44.80, 6},
			{45.03, 44.23, 45.04, 44.17, 1},
			{44.23, 43.95, 44.29, 43.81, 2},
			{43.91, 43.08, 43.99, 43.08, 1},
			{43.07, 43.55, 43.65, 43.06, 7},
			{43.56, 43.95, 43.99, 43.53, 6},
			{43.93, 44.47, 44.58, 43.93, 1},
	};


	private double accumulateAndReturn(Indicator indicator, int i) {
		Candle c;
		if (indicator instanceof VWAP) {
			c = newCandle(i, values[i][0], values[i][1], values[i][2], values[i][3], 1.0);
		} else {
			c = newCandle(i, values[i][0], values[i][1], values[i][2], values[i][3], values[i][4]);
		}
		indicator.accumulate(c);
		return indicator.getValue();
	}

	@Test
	public void vwap() {
		VWAP vwap = new VWAP(5, minutes(1));

		accumulateAndReturn(vwap, 0);
		accumulateAndReturn(vwap, 1);
		accumulateAndReturn(vwap, 2);
		accumulateAndReturn(vwap, 3);
		accumulateAndReturn(vwap, 4);
		assertEquals(45.1453, accumulateAndReturn(vwap, 5), 0.001);
		assertEquals(45.1513, accumulateAndReturn(vwap, 6), 0.001);
		assertEquals(45.1413, accumulateAndReturn(vwap, 7), 0.001);
		assertEquals(45.1547, accumulateAndReturn(vwap, 8), 0.001);
		assertEquals(45.1967, accumulateAndReturn(vwap, 9), 0.001);
		assertEquals(45.2560, accumulateAndReturn(vwap, 10), 0.001);
		assertEquals(45.3340, accumulateAndReturn(vwap, 11), 0.001);
		assertEquals(45.4060, accumulateAndReturn(vwap, 12), 0.001);
		assertEquals(45.3880, accumulateAndReturn(vwap, 13), 0.001);
		assertEquals(45.2120, accumulateAndReturn(vwap, 14), 0.001);
		assertEquals(44.9267, accumulateAndReturn(vwap, 15), 0.001);
		assertEquals(44.5033, accumulateAndReturn(vwap, 16), 0.001);
		assertEquals(44.0840, accumulateAndReturn(vwap, 17), 0.001);
		assertEquals(43.8247, accumulateAndReturn(vwap, 18), 0.001);
	}

	@Test
	public void mvwap() {
		MVWAP mvwap = new MVWAP(8, 5, minutes(1));

		accumulateAndReturn(mvwap, 0);
		accumulateAndReturn(mvwap, 1);
		accumulateAndReturn(mvwap, 2);
		accumulateAndReturn(mvwap, 3);
		accumulateAndReturn(mvwap, 4);
		accumulateAndReturn(mvwap, 5);
		accumulateAndReturn(mvwap, 6);
		accumulateAndReturn(mvwap, 7);

		assertEquals(45.1271, accumulateAndReturn(mvwap, 8), 0.001);
		assertEquals(45.1399, accumulateAndReturn(mvwap, 9), 0.001);
		assertEquals(45.1530, accumulateAndReturn(mvwap, 10), 0.001);
		assertEquals(45.1790, accumulateAndReturn(mvwap, 11), 0.001);
		assertEquals(45.2227, accumulateAndReturn(mvwap, 12), 0.001);
		assertEquals(45.2533, accumulateAndReturn(mvwap, 13), 0.001);
		assertEquals(45.2769, accumulateAndReturn(mvwap, 14), 0.001);
		assertEquals(45.2844, accumulateAndReturn(mvwap, 15), 0.001);
		assertEquals(45.2668, accumulateAndReturn(mvwap, 16), 0.001);
		assertEquals(45.1386, accumulateAndReturn(mvwap, 17), 0.001);
		assertEquals(44.9487, accumulateAndReturn(mvwap, 18), 0.001);
	}

}
