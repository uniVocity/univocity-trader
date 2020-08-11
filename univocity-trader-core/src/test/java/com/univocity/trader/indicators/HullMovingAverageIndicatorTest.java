package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HullMovingAverageIndicatorTest {

    private static final double[] values = {84.53, 87.39, 84.55, 82.83, 82.58, 83.74, 83.33, 84.57, 86.98, 87.10,
            83.11, 83.60, 83.66, 82.76, 79.22, 79.03, 78.18, 77.42, 74.65, 77.48, 76.87};

    @Test
    public void hma() {

        int i = 0;

        HullMovingAverageIndicator indicator = new HullMovingAverageIndicator(9, TimeInterval.MINUTE, c -> c.close);
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(86.3204, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(85.3705, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(84.1044, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(83.0197, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(81.3913, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(79.6511, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(78.0443, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(76.8832, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(75.5363, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(75.1713, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(75.3597, indicator.getValue(), 0.0001);

    }

}