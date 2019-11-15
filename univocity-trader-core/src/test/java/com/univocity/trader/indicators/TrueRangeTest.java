package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class TrueRangeTest {

	@Test
	public void getValue() {
		TrueRange t = new TrueRange(minutes(1));
		assertEquals(7, accumulate(t, newCandle(1, 0.0, 12, 15, 8)), 0.001);
		assertEquals(6, accumulate(t, newCandle(2, 0, 8, 11, 6)), 0.001);
		assertEquals(9, accumulate(t, newCandle(3, 0.0, 15, 17, 14)), 0.001);
		assertEquals(3, accumulate(t, newCandle(4, 0.0, 15, 17, 14)), 0.001);
		assertEquals(15, accumulate(t, newCandle(5, 0.0, 0, 0, 2)), 0.001);

	}

	@Test
	public void getValue2() {
		TrueRange t = new TrueRange(minutes(2));

		accumulate(t, newCandle(1, 0.0, 11, 15, 9));
		assertEquals(7, accumulate(t, newCandle(2, 0.0, 12, 14, 8)), 0.001);

		accumulate(t, newCandle(3, 0.0, 7, 9, 6));
		assertEquals(6, accumulate(t, newCandle(4, 0, 8, 11, 8)), 0.001);

		accumulate(t, newCandle(5, 0.0, 14, 16, 14));
		assertEquals(9, accumulate(t, newCandle(6, 0.0, 15, 17, 15)), 0.001);

		accumulate(t, newCandle(7, 0.0, 15, 17, 14));
		assertEquals(3, accumulate(t, newCandle(8, 0.0, 15, 16, 15)), 0.001);

		accumulate(t, newCandle(8, 0.0, 0, 0, 2));
		assertEquals(15, accumulate(t, newCandle(9, 0.0, 0, 0, 4)), 0.001);

	}

}