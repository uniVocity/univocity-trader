package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class ChangeIndicatorTest {

	@Test
	public void isUsable() {
		ChangeIndicator b = new ChangeIndicator(minutes(1));

		b.accumulate(newCandle(1, 1.0));
		b.accumulate(newCandle(2, 2.0));

		assertEquals(100.0, b.getValue(), 0.001);

		b.accumulate(newCandle(3, 3.0));
		assertEquals(50.0, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 2.0));
		assertEquals(-33.333333, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 3.0));
		assertEquals(50.0, b.getValue(), 0.001);

		b = new ChangeIndicator(minutes(2));
		b.recalculateEveryTick(true);

		b.accumulate(newCandle(1, 2.0));
		b.accumulate(newCandle(2, 1.0));
		assertEquals(0.0, b.getValue(), 0.001); //no change, first candle accumulated
		//end candle 1

		b.accumulate(newCandle(3, 3.0)); //change against closing $1.0 of previous candle
		assertEquals(200.0, b.getValue(), 0.001);

		b.accumulate(newCandle(4, 2.5)); //still against closing $1.0 of previous candle. This candle is closed.
		assertEquals(150.0, b.getValue(), 0.001);
		//end candle 2

		b.accumulate(newCandle(5, 2.0));
		assertEquals(-19.999, b.getValue(), 0.001);

		b.accumulate(newCandle(6, 3.0));
		assertEquals(19.999, b.getValue(), 0.001);
		//end candle 3
	}

}