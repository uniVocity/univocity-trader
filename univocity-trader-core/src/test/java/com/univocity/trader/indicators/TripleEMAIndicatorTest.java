package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TripleEMAIndicatorTest {

    private double[] values = {0.73, 0.72, 0.86, 0.72, 0.62, 0.76, 0.84, 0.69, 0.65, 0.71,
            0.53, 0.73, 0.77, 0.67, 0.68};

    @Test
    public void tripleEMAUsingBarCount5UsingClosePrice() {

        int i = 0;

        TripleEMAIndicator indicator = new TripleEMAIndicator(5, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.73, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.7229, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.8185, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.8027, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.7328, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.6725, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.7386, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.6994, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.6876, indicator.getValue(), 0.0001);

    }

}
