package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VolumeIndicatorTest {

    private static final double[] values = {10, 11, 12, 13, 150, 155, 160};

    @Test
    public void volume() {

        final double ZERO = 0;

        int i = 0;

        VolumeIndicator indicator = new VolumeIndicator(3, TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(10, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(21, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(33, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(36, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(175, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(318, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, ZERO, ZERO, ZERO, ZERO, values[i++]));
        assertEquals(465, indicator.getValue(), 0.0001);

    }

}
