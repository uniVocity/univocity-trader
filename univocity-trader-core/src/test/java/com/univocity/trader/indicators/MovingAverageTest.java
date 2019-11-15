package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class MovingAverageTest {

	@Test
	public void isUsable() {
		MovingAverage ma = new MovingAverage(5, minutes(1));

		assertEquals(1.0, accumulate(ma, 1, 1.0), 0.001);
		assertEquals(1.5, accumulate(ma, 2, 2.0), 0.001);
		assertEquals(1.3333, accumulate(ma, 3, 1.0), 0.001);
		assertEquals(1.5, accumulate(ma, 4, 2.0), 0.001);
		assertEquals(1.6, accumulate(ma, 5, 2.0), 0.001);
		assertEquals(1.8, accumulate(ma, 6, 2.0), 0.001);
		assertEquals(1.8, accumulate(ma, 7, 2.0), 0.001);
		assertEquals(2.0, accumulate(ma, 8, 2.0), 0.001);

		// 2 min
		ma = new MovingAverage(2, minutes(2));
		ma.recalculateEveryTick(true);

		assertEquals(1.0, accumulate(ma, 1, 1.0), 0.001);
		assertEquals(2.0, accumulate(ma, 2, 2.0), 0.001);

		assertEquals(1.5, accumulate(ma, 3, 1.0), 0.001);
		assertEquals(1.75, accumulate(ma, 4, 1.5), 0.001);

		assertEquals(1.25, accumulate(ma, 5, 1.0), 0.001);
		assertEquals(3.25, accumulate(ma, 6, 5.0), 0.001);

		assertEquals(5.0, accumulate(ma, 7, 5.0), 0.001);
		assertEquals(3.0, accumulate(ma, 8, 1.0), 0.001);

		// 2 min
		ma = new MovingAverage(2, minutes(2));
		ma.recalculateEveryTick(true);

		assertEquals(1.0, accumulate(ma, 3, 1.0), 0.001); //update
		assertEquals(1.5, accumulate(ma, 3, 1.5), 0.001); //update again, same instant
		assertEquals(1.5, accumulate(ma, 2, 2.0), 0.001); //noop, previous instant
		assertEquals(9.0, accumulate(ma, 4, 9.0), 0.001);

		// 2 min
		ma = new MovingAverage(2, minutes(2));
		ma.recalculateEveryTick(true);

		assertEquals(1.0, update(ma, 1, 1.0), 0.001);
		assertEquals(2.0, update(ma, 2, 2.0), 0.001);

		assertEquals(1.5, update(ma, 3, 1.0), 0.001);
		assertEquals(1.75, update(ma, 4, 1.5), 0.001);

		assertEquals(1.25, update(ma, 5, 1.0), 0.001);
		assertEquals(3.25, update(ma, 6, 5.0), 0.001);

		assertEquals(5.0, update(ma, 7, 5.0), 0.001);
		assertEquals(3.0, update(ma, 8, 1.0), 0.001);


		// 3 min
		ma = new MovingAverage(2, minutes(3));
		ma.recalculateEveryTick(true);

		assertEquals(1.0, update(ma, 1, 1.0), 0.001);
		assertEquals(2.0, update(ma, 2, 2.0), 0.001);
		assertEquals(1.0, update(ma, 3, 1.0), 0.001);

		assertEquals(1.25, update(ma, 4, 1.5), 0.001);
		assertEquals(1.0, update(ma, 5, 1.0), 0.001);
		assertEquals(3.0, update(ma, 6, 5.0), 0.001);

		assertEquals(5.0, update(ma, 7, 5.0), 0.001);
		assertEquals(3.0, update(ma, 8, 1.0), 0.001);
		assertEquals(3.5, update(ma, 9, 2.0), 0.001);
	}
}
