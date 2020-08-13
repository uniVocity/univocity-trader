package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MeanDeviationIndicatorTest {

    private static final double[] values = {1, 2, 7, 6, 3, 4, 5, 11, 3, 0, 9};

    @Test
    public void mean() {

        int i = 0;

        MeanDeviationIndicator indicator = new MeanDeviationIndicator(5, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.44444444444444, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.5, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.16, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.32, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.72, indicator.getValue(), 0.001);

    }

}
