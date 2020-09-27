package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class DojiTest {

	@Test
	public void getValueAtIndex0() {

		int i = 0;

		Doji indicator = new Doji(0.03, 10, TimeInterval.MINUTE);
		indicator.accumulate(CandleHelper.newCandle(i++, 0));
		assertEquals(1, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 1));
		assertEquals(0, indicator.getValue(), 0.0001);

	}

	@Test
	public void getValue() {

		int i = 0;

		Doji indicator = new Doji(0.03, 10, TimeInterval.MINUTE);

		indicator.accumulate(CandleHelper.newCandle(i++, 19, 19, 22, 16));
		assertEquals(1, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 10));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 17, 20, 21, 17));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 15, 15.1, 16, 14));
		assertEquals(1, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 15, 11, 15, 8));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 11, 12, 12, 10));
		assertEquals(0, indicator.getValue(), 0.0001);

	}

}