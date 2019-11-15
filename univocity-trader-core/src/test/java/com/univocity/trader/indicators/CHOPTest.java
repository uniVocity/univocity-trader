package com.univocity.trader.indicators;



import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class CHOPTest {

	/**
	 * this will assert that choppiness is high if market price is not moving
	 */
	@Test
	public void testChoppy() {
		CHOP ind = new CHOP(minutes(1));

		for (int i = 0; i < 50; i++) {
			Candle c = newCandle(i, 21.5, 21.5, 21.5 + 1.0, 21.5 - 1.0);
			ind.accumulate(c);
		}

		int HIGH_CHOPPINESS_VALUE = 85;
		System.out.println(ind.getValue());
		assertTrue(ind.getValue() <= 100.0);
		assertTrue(ind.getValue() >= 0.0);
		System.out.println(ind.getValue());
		assertTrue(ind.getValue() > HIGH_CHOPPINESS_VALUE);
	}


	/**
	 * this will assert that choppiness is low if market price is trending significantly
	 */
	@Test
	public void testTradeable() {

		CHOP ind = new CHOP(minutes(1));

		double value = 21.5;
		for (int i = 0; i < 50; i++) {
//			ZonedDateTime date = ZonedDateTime.now().minusSeconds(100000 - (i * MINUTE));
//			c.setOpenTime(date.toInstant().toEpochMilli());

			Candle c = newCandle(i, value, value, value + 1.0, value - 1.0);

			ind.accumulate(c);
			value += 2.0f;
		}

		int LOW_CHOPPINESS_VALUE = 30;
		//should be 15.364012882860568
		assertTrue(ind.getValue() <= 100.0);
		assertTrue(ind.getValue() >= 0.0);
		assertTrue(ind.getValue() < LOW_CHOPPINESS_VALUE);
		assertEquals(ind.getValue(), 15.364, 0.0001);
	}
}

