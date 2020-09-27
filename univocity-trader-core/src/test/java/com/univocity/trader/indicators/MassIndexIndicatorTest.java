package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MassIndexIndicatorTest {

    @Test
    public void mass() {

        int i = 0;

        MassIndex indicator = new MassIndex(3, 8, TimeInterval.minutes(1));

        indicator.accumulate(CandleHelper.newCandle(i++, 44.98, 45.05, 45.17, 44.96));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 45.05, 45.10, 45.15, 44.99));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.11, 45.19, 45.32, 45.11));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.19, 45.14, 45.25, 45.04));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.12, 45.15, 45.20, 45.10));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.15, 45.14, 45.20, 45.10));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.13, 45.10, 45.16, 45.07));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.12, 45.15, 45.22, 45.10));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.15, 45.22, 45.27, 45.14));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.24, 45.43, 45.45, 45.20));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.43, 45.44, 45.50, 45.39));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.43, 45.55, 45.60, 45.35));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.58, 45.55, 45.61, 45.39));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.45, 45.01, 45.55, 44.80));
        indicator.accumulate(CandleHelper.newCandle(i++, 45.03, 44.23, 45.04, 44.17));
        assertEquals(9.1158, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 44.23, 43.95, 44.29, 43.81));
        assertEquals(9.2462, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 43.91, 43.08, 43.99, 43.08));
        assertEquals(9.4026, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 43.07, 43.55, 43.65, 43.06));
        assertEquals(9.2129, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 43.56, 43.95, 43.99, 43.53));
        assertEquals(9.1576, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 43.93, 44.47, 44.58, 43.93));
        assertEquals(9.0184, indicator.getValue(), 0.0001);


    }

}
