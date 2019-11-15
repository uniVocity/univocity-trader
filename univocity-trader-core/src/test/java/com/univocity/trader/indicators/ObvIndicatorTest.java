package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class ObvIndicatorTest {


	@Test
	public void test1Min() {
		OBV t = new OBV(minutes(1));

		assertEquals(0, accumulate(t, newCandle(1, 10, 10.00, 10, 10, 25_200.0)), 0.001);
		assertEquals(30_000, accumulate(t, newCandle(2, 10, 10.15, 10, 10, 30_000.0)), 0.001);
		assertEquals(55_600, accumulate(t, newCandle(3, 10, 10.17, 10, 10, 25_600.0)), 0.001);
		assertEquals(23_600, accumulate(t, newCandle(4, 10, 10.13, 10, 10, 32_000.0)), 0.001);
		assertEquals(600, accumulate(t, newCandle(5, 10, 10.11, 10, 10, 23_000.0)), 0.001);
		assertEquals(40_600, accumulate(t, newCandle(6, 10, 10.15, 10, 10, 40_000.0)), 0.001);
		assertEquals(76_600, accumulate(t, newCandle(7, 10, 10.20, 10, 10, 36_000.0)), 0.001);
		assertEquals(76_600, accumulate(t, newCandle(8, 10, 10.20, 10, 10, 20_500.0)), 0.001);
		assertEquals(99_600, accumulate(t, newCandle(9, 10, 10.22, 10, 10, 23_000.0)), 0.001);
		assertEquals(72_100, accumulate(t, newCandle(10, 10, 10.21, 10, 10, 27_500.0)), 0.001);
	}

	@Test
	public void test2Min() {
		OBV t = new OBV(minutes(2));
		t.recalculateEveryTick(true);

		assertEquals(0, accumulate(t, newCandle(1, 10, 10.00, 10, 10, 25_200.0)), 0.001);
		assertEquals(0, accumulate(t, newCandle(2, 10, 10.15, 10, 10, 30_000.0)), 0.001);

		assertEquals(25_600.0, accumulate(t, newCandle(3, 10, 10.17, 10, 10, 25_600.0)), 0.001);
		assertEquals(-57_600, accumulate(t, newCandle(4, 10, 10.13, 10, 10, 32_000.0)), 0.001); //sum of volumes of both candles, i.e. 25_600.0 + 32_000.0

		assertEquals(-80_600, accumulate(t, newCandle(5, 10, 10.11, 10, 10, 23_000.0)), 0.001);
		assertEquals(5_400, accumulate(t, newCandle(6, 10, 10.15, 10, 10, 40_000.0)), 0.001);

		assertEquals(41_400, accumulate(t, newCandle(7, 10, 10.20, 10, 10, 36_000.0)), 0.001);
		assertEquals(61_900, accumulate(t, newCandle(8, 10, 10.20, 10, 10, 20_500.0)), 0.001);

		assertEquals(84_900, accumulate(t, newCandle(9, 10, 10.22, 10, 10, 23_000.0)), 0.001);
		assertEquals(112_400, accumulate(t, newCandle(10, 10, 10.21, 10, 10, 27_500.0)), 0.001);
	}
}