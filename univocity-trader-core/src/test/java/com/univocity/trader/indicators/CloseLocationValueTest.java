package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CloseLocationValueTest {

    @Test
    public void closeLocation() {

        int i = 0;

        CloseLocationValue indicator = new CloseLocationValue(TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 10));
        assertEquals(0.6, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 17, 20, 21, 17));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 15, 16, 14));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 11, 15, 8));
        assertEquals(-1d / 7, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 11, 12, 12, 10));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 10, 10, 10, 10));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 11, 12, 12, 10));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 11, 120, 140, 100));
        assertEquals(0, indicator.getValue(), 0.0001);

    }

}
