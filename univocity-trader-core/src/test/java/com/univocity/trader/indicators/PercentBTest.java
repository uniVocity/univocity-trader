package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PercentBTest {


    private double[] values = {10, 12, 15, 14, 17, 20, 21, 20, 20, 19, 20, 17, 12, 12, 9, 8, 9, 10, 9, 10};

    @Test
    public void percentBUsingSMAAndStandardDeviation() {

        int i = 0;

        PercentB indicator = new PercentB(5, 2, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Double.NaN, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(0.75, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(0.8244, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(0.6627, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.8517, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.90328, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.83, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.6552, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5737, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.1047, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.0284, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.0344, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.2064, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.1835, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.2131, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.3506, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5737, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.7673, indicator.getValue(), 0.0001);

    }

}
