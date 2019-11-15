package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class ZeroLagMovingAverageTest {

	@Test
	public void ZLEMAUsingBarCount10UsingClosePrice() {
		ZeroLagMovingAverage zlema = new ZeroLagMovingAverage(10, minutes(1));

		assertEquals(10.000, accumulate(zlema, 0, 10), 0.001);
		assertEquals(10.909, accumulate(zlema, 1, 15), 0.001);
		assertEquals(13.471, accumulate(zlema, 2, 20), 0.001);
		assertEquals(14.839, accumulate(zlema, 3, 18), 0.001);
		assertEquals(15.596, accumulate(zlema, 4, 17), 0.001);
		assertEquals(15.669, accumulate(zlema, 5, 18), 0.001);
		assertEquals(15.002, accumulate(zlema, 6, 15), 0.001);
		assertEquals(13.547, accumulate(zlema, 7, 12), 0.001);
		assertEquals(11.447, accumulate(zlema, 8, 10), 0.001);
		assertEquals(9.548, accumulate(zlema, 9, 8), 0.001);
		assertEquals(07.448, accumulate(zlema, 10, 5), 0.001);
		assertEquals(05.003, accumulate(zlema, 11, 2), 0.001);
	}
}
