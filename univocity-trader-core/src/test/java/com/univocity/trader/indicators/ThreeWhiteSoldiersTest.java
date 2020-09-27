package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class ThreeWhiteSoldiersTest {

	@Test
	public void threeWhiteSoldiersIndicator() {

		int i = 0;

		ThreeWhiteSoldiers indicator = new ThreeWhiteSoldiers(3, 0.1, TimeInterval.MINUTE);

		indicator.accumulate(CandleHelper.newCandle(i++, 19, 19, 22, 15));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 8));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 17, 16, 21, 15));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 15.6, 18, 18.1, 14));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 16, 19.9, 20, 15));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 16.8, 23, 23, 16.7));
        assertEquals(Signal.BUY, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 17, 25, 25, 17));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

		indicator.accumulate(CandleHelper.newCandle(i++, 23, 16.8, 24, 15));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

	}

}
