package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VarianceTest {

    private static final double[] values = {1, 2, 3, 4, 3, 4, 5, 4, 3, 0, 9};

    @Test
    public void varianceUsingBarCount4UsingClosePrice() {

        int i = 0;

        Variance indicator = new Variance(4, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.0 / 3, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(1.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(3.5, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(10.5, indicator.getValue(), 0.0001);

    }

    @Test
    public void firstValueShouldBeZero() {

        int i = 0;

        Variance indicator = new Variance(4, TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);
    }

    @Test
    public void varianceShouldBeZeroWhenBarCountIs1() {

        int i = 0;

        Variance indicator = new Variance(1, TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

    }

    @Test
    public void varianceUsingBarCount2UsingClosePrice() {

        int i = 0;

        Variance indicator = new Variance(2, TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.25, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(20.25, indicator.getValue(), 0.0001);

    }

}
