package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class DoubleExponentialMovingAverageTest {

	@Test
	public void doubleEMAUsingBarCount5UsingClosePrice() {
		DoubleExponentialMovingAverage doubleEma = new DoubleExponentialMovingAverage(5, minutes(1));

		assertEquals(0.73, accumulate(doubleEma, 0, 0.73), 0.0001);
		assertEquals(0.7244, accumulate(doubleEma, 1, 0.72), 0.0001);
		assertEquals(0.7992, accumulate(doubleEma, 2, 0.86), 0.0001);
		accumulate(doubleEma, 3, 0.72);
		accumulate(doubleEma, 4, 0.62);
		accumulate(doubleEma, 5, 0.76);
		assertEquals(0.7858, accumulate(doubleEma, 6, 0.84), 0.0001);
		assertEquals(0.7374, accumulate(doubleEma, 7, 0.69), 0.0001);
		assertEquals(0.6884, accumulate(doubleEma, 8, 0.65), 0.0001);
		accumulate(doubleEma, 9, 0.71);
		accumulate(doubleEma, 10, 0.53);
		accumulate(doubleEma, 11, 0.73);
		assertEquals(0.7184, accumulate(doubleEma, 12, 0.77), 0.0001);
		assertEquals(0.6939, accumulate(doubleEma, 13, 0.67), 0.0001);
		assertEquals(0.6859, accumulate(doubleEma, 14, 0.68), 0.0001);
	}
}