package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import org.junit.Test;

import static com.univocity.trader.indicators.base.TimeInterval.minutes;
import static org.junit.Assert.assertEquals;

public class RangeActionVerificationIndexTest {

    @Test
    public void ravi() {

        int i = 0;

        RangeActionVerificationIndex indicator = new RangeActionVerificationIndex(3, 8, minutes(1));

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 110, 0, 0));
        assertEquals(0D, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 109.27, 0, 0));
        assertEquals(0D, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 104.69, 0, 0));
        assertEquals(0D, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 107.07, 0, 0));
        assertEquals(-0.6937D, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 107.92, 0, 0));
        assertEquals(-1.1411, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 107.95, 0, 0));
        assertEquals(-0.1577, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 108.70, 0, 0));
        assertEquals(0.229, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 107.97, 0, 0));
        assertEquals(0.2412, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 106.09, 0, 0));
        assertEquals(0.1202, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 106.03, 0, 0));
        assertEquals(-0.3324, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 108.65, 0, 0));
        assertEquals(-0.5804, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 109.54, 0, 0));
        assertEquals(0.2013, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 112.26, 0, 0));
        assertEquals(1.6156, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 114.38, 0, 0));
        assertEquals(2.6167, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 117.94, 0, 0));
        assertEquals(4.0799, indicator.getValue(), 0.001);

    }

}
