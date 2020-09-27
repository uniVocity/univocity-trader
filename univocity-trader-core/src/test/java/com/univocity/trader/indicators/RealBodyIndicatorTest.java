package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import org.junit.Test;

import static com.univocity.trader.indicators.base.TimeInterval.minutes;
import static org.junit.Assert.assertEquals;

public class RealBodyIndicatorTest {

    @Test
    public void test() {

        RealBodyIndicator indicator = new RealBodyIndicator(minutes(1));

        indicator.process(CandleHelper.newCandle(0, 10, 18, 20, 10), 0, false);
        assertEquals(8D, indicator.getValue(), 0);

        indicator.process(CandleHelper.newCandle(0, 17, 20, 21, 17), 0, false);
        assertEquals(3D, indicator.getValue(), 0);

        indicator.process(CandleHelper.newCandle(0, 15, 15, 16, 14), 0, false);
        assertEquals(0D, indicator.getValue(), 0);

        indicator.process(CandleHelper.newCandle(0, 15, 11, 15, 8), 0, false);
        assertEquals(-4D, indicator.getValue(), 0);

        indicator.process(CandleHelper.newCandle(0, 11, 12, 12, 10), 0, false);
        assertEquals(1D, indicator.getValue(), 0);


    }

}
