package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreeBlackCrowsIndicatorTest {

    @Test
    public void threeBlackCrowsIndicator() {

        int i = 0;

        ThreeBlackCrowsIndicator indicator = new ThreeBlackCrowsIndicator(3, 0.1, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, 19, 19, 22, 15));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 8));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 17, 20, 21, 17));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 19, 17, 20, 16.9));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 17.5, 14, 18, 13.9));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 11, 15, 11));
        assertEquals(1, indicator.getValue(), 0.0001);


    }

}
