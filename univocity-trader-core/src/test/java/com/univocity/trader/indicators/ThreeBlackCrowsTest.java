package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreeBlackCrowsTest {

    @Test
    public void threeBlackCrowsIndicator() {

        int i = 0;

        ThreeBlackCrows indicator = new ThreeBlackCrows(3, 0.1, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, 19, 19, 22, 15));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 8));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 17, 20, 21, 17));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 19, 17, 20, 16.9));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 17.5, 14, 18, 13.9));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 11, 15, 11));
        assertEquals(Signal.SELL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 12, 14, 15, 8));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));

        indicator.accumulate(CandleHelper.newCandle(i++, 13, 16, 16, 11));
        assertEquals(Signal.NEUTRAL, indicator.getSignal(null));


    }

}
