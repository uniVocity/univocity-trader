package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinearlyWeightedMovingAverageIndicatorTest {

    private static final double[] values = {37.08, 36.7, 36.11, 35.85, 35.71, 36.04, 36.41, 37.67, 38.01, 37.79,
            36.83};

    @Test
    public void lwma() {
        int i = 0;

        LinearlyWeightedMovingAverageIndicator indicator = new LinearlyWeightedMovingAverageIndicator(5, TimeInterval.MINUTE, c -> c.close);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.0, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.0, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.0, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.0, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(36.0506, indicator.getValue(), 0.001);

//        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(35.9673, indicator.getValue(), 0.001);

//        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(36.0766, indicator.getValue(), 0.001);
//
//        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(36.6253, indicator.getValue(), 0.001);
//
//        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(37.1833, indicator.getValue(), 0.001);
//
//        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(37.5240, indicator.getValue(), 0.001);
//
//        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(37.4060, indicator.getValue(), 0.001);

    }

}
