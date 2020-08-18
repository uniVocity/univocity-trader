package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TRIndicatorTest {

    @Test
    public void tr() {

        int i = 0;

        TRIndicator indicator = new TRIndicator(TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 12, 15, 8));
        assertEquals(7, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 8, 11, 6));
        assertEquals(6, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 15, 17, 14));
        assertEquals(9, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 15, 17, 14));
        assertEquals(3, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 0, 0, 2));
        assertEquals(15, indicator.getValue(), 0.001);

    }

}
