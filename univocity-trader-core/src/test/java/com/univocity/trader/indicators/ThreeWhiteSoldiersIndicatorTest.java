package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreeWhiteSoldiersIndicatorTest {

    @Test
    public void threeWhiteSoldiersIndicator() {

        int i = 0;

        ThreeWhiteSoldiersIndicator indicator =
                new ThreeWhiteSoldiersIndicator(3, 0.01, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, 19, 19, 22, 15));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  10, 18, 20, 8));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  17, 16, 21, 15));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  15.6, 18, 18.1, 14));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  16, 19.9, 20, 15));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  16.8, 23, 23, 16.7));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  17, 25, 25, 17));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++,  23, 16.8, 24, 15));
        assertEquals(0, indicator.getValue(), 0.0001);

    }

}
