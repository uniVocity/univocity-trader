package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class BollingerBandTest {

	@Test
	public void isUsable() {
		BollingerBand b = new BollingerBand(5, minutes(1));

		accumulate(b, 1, 25.5);
		accumulate(b, 2, 26.75);
		accumulate(b, 3,  27.0);
		accumulate(b, 4, 26.5);
		accumulate(b, 5, 27.25);
		assertEquals(b.getMiddleBand(), 26.6, 0.1);
		assertEquals(b.getUpperBand(), 27.808, 0.1);
		assertEquals(b.getLowerBand(), 25.392, 0.1);


		b = new BollingerBand(5, minutes(2));
		accumulate(b, 1, 29.5);
		accumulate(b, 2, 25.5);
		accumulate(b, 3, 23.75);
		accumulate(b, 4, 26.75);
		accumulate(b, 5,  23.34);
		accumulate(b, 6,  27.0);
		accumulate(b, 7, 26.6);
		accumulate(b, 8, 26.5);
		accumulate(b, 9, 27.15);
		accumulate(b, 10, 27.25);
		assertEquals(b.getMiddleBand(), 26.6, 0.1);
		assertEquals(b.getUpperBand(), 27.808, 0.1);
		assertEquals(b.getLowerBand(), 25.392, 0.1);
	}
}