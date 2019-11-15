package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;


public class HighestValueIndicatorTest {

	@Test
	public void isUsable() {
		HighestValueIndicator b = new HighestValueIndicator(3, minutes(1), c -> c.close);

		assertEquals(1.0, accumulate(b, 0, 1.0), 0.1);
		assertEquals(1.5, accumulate(b, 1, 1.5), 0.1);
		assertEquals(2.0, accumulate(b, 2, 2.0), 0.1);
		assertEquals(2.0, accumulate(b, 3, 1.2), 0.1);
		assertEquals(2.0, accumulate(b, 4, 1.0), 0.1);
		assertEquals(1.2, accumulate(b, 5, 1.0), 0.1);
		assertEquals(1.0, accumulate(b, 6, 1.0), 0.1);
		assertEquals(1.1, accumulate(b, 7, 1.1), 0.1);

		b = new HighestValueIndicator(3, minutes(2), c -> c.close);
		b.recalculateEveryTick(true);

		assertEquals(1.0, accumulate(b, 0, 1.0), 0.1);
		assertEquals(1.5, accumulate(b, 1, 1.5), 0.1);

		assertEquals(2.0, accumulate(b, 2, 2.0), 0.1);
		assertEquals(1.5, accumulate(b, 3, 1.2), 0.1);

		assertEquals(1.5, accumulate(b, 4, 1.0), 0.1);
		assertEquals(1.5, accumulate(b, 5, 1.0), 0.1);

		assertEquals(1.7, accumulate(b, 6, 1.7), 0.1);
		assertEquals(1.6, accumulate(b, 7, 1.6), 0.1);

		assertEquals(1.6, accumulate(b, 8, 1.0), 0.1);
		assertEquals(1.6, accumulate(b, 9, 1.1), 0.1);
	}
}