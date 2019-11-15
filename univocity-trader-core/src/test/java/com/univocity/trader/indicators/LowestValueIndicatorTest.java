package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class LowestValueIndicatorTest {

	@Test
	public void isUsable() {
		LowestValueIndicator b = new LowestValueIndicator(3, minutes(1), c -> c.close);

		assertEquals(2.0, accumulate(b, 0, 2.0), 0.1);
		assertEquals(1.5, accumulate(b, 1, 1.5), 0.1);
		assertEquals(1.0, accumulate(b, 2, 1.0), 0.1);

		assertEquals(1.0, accumulate(b, 3, 1.2), 0.1);
		assertEquals(1.0, accumulate(b, 4, 2.0), 0.1);
		assertEquals(1.2, accumulate(b, 5, 2.0), 0.1);

		assertEquals(1.5, accumulate(b, 6, 1.5), 0.1);
		assertEquals(1.5, accumulate(b, 7, 1.7), 0.1);
		assertEquals(1.1, accumulate(b, 8, 1.1), 0.1);

		b = new LowestValueIndicator(3, minutes(2), c -> c.close);
		b.recalculateEveryTick(true);

		assertEquals(1.5, accumulate(b, 0, 1.5), 0.1);
		assertEquals(1.0, accumulate(b, 1, 1.0), 0.1);

		assertEquals(0.9, accumulate(b, 2, 0.9), 0.1);
		assertEquals(1.0, accumulate(b, 3, 2.0), 0.1);

		assertEquals(1.0, accumulate(b, 4, 1.1), 0.1);
		assertEquals(1.0, accumulate(b, 5, 1.2), 0.1);

		assertEquals(0.8, accumulate(b, 6, 0.8), 0.1);
		assertEquals(1.2, accumulate(b, 7, 1.3), 0.1);

		assertEquals(1.0, accumulate(b, 8, 1.0), 0.1);
		assertEquals(1.2, accumulate(b, 9, 1.3), 0.1);

		assertEquals(1.1, accumulate(b, 10, 1.1), 0.1);
		assertEquals(1.3, accumulate(b, 11, 1.7), 0.1);


		b = new LowestValueIndicator(5, minutes(1), c -> c.low);
		assertEquals(44.96, update(b, 1, 44.96), 0.1);
		assertEquals(44.96, update(b, 1, 44.99), 0.1);
		assertEquals(44.96, update(b, 1, 45.11), 0.1);
		assertEquals(44.96, update(b, 1, 45.04), 0.1);
		assertEquals(44.96, update(b, 1, 45.10), 0.1);
		assertEquals(44.99, update(b, 1, 45.10), 0.1);
		assertEquals(45.04, update(b, 1, 45.07), 0.1);
		assertEquals(45.04, update(b, 1, 45.10), 0.1);
		assertEquals(45.07, update(b, 1, 45.14), 0.1);
		assertEquals(45.07, update(b, 1, 45.20), 0.1);
		assertEquals(45.07, update(b, 1, 45.39), 0.1);
		assertEquals(45.10, update(b, 1, 45.35), 0.1);
		assertEquals(45.14, update(b, 1, 45.39), 0.1);
		assertEquals(44.80, update(b, 1, 44.80), 0.1);
		assertEquals(44.17, update(b, 1, 44.17), 0.1);
	}

	@Test
	public void test2Minutes(){
		LowestValueIndicator b = new LowestValueIndicator(3, minutes(2), c -> c.low);

		b.update(newCandle(1, 1));
		b.update(newCandle(2, 2));
		assertEquals(1.0, b.getValue());
		//end candle 1

		b.update(newCandle(3, 3));
		b.update(newCandle(4, 4));
		assertEquals(1.0, b.getValue());
		//end candle 2

		b.update(newCandle(5, 3));
		b.update(newCandle(6, 4));
		assertEquals(1.0, b.getValue());
		//end candle 3

		b.update(newCandle(7, 5));
		b.update(newCandle(8, 4));
		assertEquals(3.0, b.getValue());
		//end candle 4

		b.update(newCandle(9, 3));
		b.update(newCandle(10, 3));
		assertEquals(3.0, b.getValue());
		//end candle 5

		b.update(newCandle(11, 4));
		b.update(newCandle(12, 3));
		assertEquals(3.0, b.getValue());
		//end candle 6

		b.update(newCandle(13, 2));
		b.update(newCandle(14, 1));
		assertEquals(1.0, b.getValue());
		//end candle 7

	}
}