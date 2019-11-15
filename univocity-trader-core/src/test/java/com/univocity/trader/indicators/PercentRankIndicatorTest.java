package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class PercentRankIndicatorTest {

	@Test
	public void isUsable() {
//		Percent rank is the percentage of values that are less than the last X values.
//		So if we have 20 values in a set, and the average is 2.0%, and there are only 3 values in the set that are less than 2.0%

//		Percent Rank = 3 / 20 = 0.15 = 15%

		PercentRankIndicator b = new PercentRankIndicator(10, minutes(1));

		b.accumulate(newCandle(1, 1.0));
		b.accumulate(newCandle(2, 2.0));

		assertEquals(50.0, b.getValue(), 0.001);

		b.accumulate(newCandle(3, 3.0));
		assertEquals(33.3333, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 2.0));
		assertEquals(0.0, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 3.0));
		assertEquals(40.0, b.getValue(), 0.001);

		b.accumulate(newCandle(5, 10.0));
		assertEquals(83.3333, b.getValue(), 0.001);

		b = new PercentRankIndicator(minutes(2));
		b.recalculateEveryTick(true);

		b.accumulate(newCandle(1, 1.0));
		b.accumulate(newCandle(2, 2.0));

		assertEquals(0.0, b.getValue(), 0.001);

		b.accumulate(newCandle(3, 3.0));
		assertEquals(50.0, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 2.5));
		assertEquals(50, b.getValue(), 0.001);

		b.accumulate(newCandle(5, 2.5));
		assertEquals(0.0, b.getValue(), 0.001);

	}
}