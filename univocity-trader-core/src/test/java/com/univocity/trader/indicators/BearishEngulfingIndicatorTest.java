package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BearishEngulfingIndicatorTest {

    @Test
    public void bearishEngulfing() {

        int i = 0;

        BearishEngulfingIndicator indicator = new BearishEngulfingIndicator(TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 10));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 17, 20, 21, 17));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 21, 15, 22, 14));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 11, 15, 8));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 11, 12, 12, 10));
        assertEquals(0, indicator.getValue(), 0.0001);

    }

}
