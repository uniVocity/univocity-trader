package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class IchimokuChikouSpanTest {

	@Test
	public void testCalculateWithDefaultParam() {

		int timeDelay = 26;

		IchimokuChikouSpan indicator = new IchimokuChikouSpan(timeDelay, TimeInterval.MINUTE);

		int i;
		for (i = 0; i < 26; i++) {
			indicator.accumulate(CandleHelper.newCandle(i, i));
			assertEquals(0.0, indicator.getValue(), 0.0001);
		}

		indicator.accumulate(CandleHelper.newCandle(i++, i++));
		assertEquals(1.0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, i++));
		assertEquals(2.0, indicator.getValue(), 0.0001);
	}

}
