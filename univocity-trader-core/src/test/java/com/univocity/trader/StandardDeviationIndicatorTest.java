package com.univocity.trader;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.StandardDeviationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandardDeviationIndicatorTest {

    double values[] = {1, 2, 3, 4, 3, 4, 5, 4, 3, 0, 9};

    @Test
    public void standardDeviationUsingBarCount4UsingClosePrice() {

        int i = 0;

        StandardDeviationIndicator indicator = new StandardDeviationIndicator(4, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(0.25), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(2.0 / 3), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(1.25), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(0.25), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(3.5), indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(Math.sqrt(10.5), indicator.getValue(), 0.0001);

    }

    @Test
    public void standardDeviationShouldBeZeroWhenBarCountIs1() {

        int i = 0;

        StandardDeviationIndicator indicator = new StandardDeviationIndicator(1, TimeInterval.MINUTE);

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
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

    }

}
