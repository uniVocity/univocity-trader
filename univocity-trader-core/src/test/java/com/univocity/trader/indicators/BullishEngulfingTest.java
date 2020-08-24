package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BullishEngulfingTest {

    @Test
    public void bullishEngulfingIndicator() {

        int i = 0;

        BullishEngulfing indicator = new BullishEngulfing(TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, 10, 18, 20, 10));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 17, 16, 19, 15));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 18, 19, 14));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 15, 11, 15, 8));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 11, 12, 12, 10));
        assertEquals(0, indicator.getValue(), 0.0001);

    }

}
