package com.univocity.trader.indicators;



import org.junit.*;


import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class StreakIndicatorTest {


	@Test
	public void isUsable() {
		StreakIndicator b = new StreakIndicator(minutes(1));

		b.accumulate(newCandle(1, 20.0));
		assertEquals(0, b.getValue(), 0.001);

		b.accumulate(newCandle(2, 20.50));
		assertEquals(1, b.getValue(), 0.001);

		b.accumulate(newCandle(3, 20.75));
		assertEquals(2, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 19.75));
		assertEquals(-1, b.getValue(), 0.001);

		b.accumulate(newCandle(5, 19.50));
		assertEquals(-2, b.getValue(), 0.001);

		b.accumulate(newCandle(6, 19.35));
		assertEquals(-3, b.getValue(), 0.001);

		b.accumulate(newCandle(7, 19.35));
		assertEquals(0, b.getValue(), 0.001);

		b.accumulate(newCandle(8, 19.40));
		assertEquals(1, b.getValue(), 0.001);



	}

}