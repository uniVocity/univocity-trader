package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AroonUpIndicatorTest {

    @Test
    public void arron() {

        int i = 0;

        AroonUpIndicator indicator = new AroonUpIndicator(20, TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i++, 168.28, 169.87, 167.15, 169.64));
        indicator.accumulate(CandleHelper.newCandle(i++, 168.84, 169.36, 168.2, 168.71));
        indicator.accumulate(CandleHelper.newCandle(i++, 168.88, 169.29, 166.41, 167.74));
        indicator.accumulate(CandleHelper.newCandle(i++, 168, 168.38, 166.18, 166.32));
        indicator.accumulate(CandleHelper.newCandle(i++, 166.89, 167.7, 166.33, 167.24));
        indicator.accumulate(CandleHelper.newCandle(i++, 165.25, 168.43, 165, 168.05));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 168.17, 170.18, 167.63, 169.92));
        assertEquals(100, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 170.42, 172.15, 170.06, 171.97));
        assertEquals(100, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 172.41, 172.92, 171.31, 172.02));
        assertEquals(100, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 171.2, 172.39, 169.55, 170.72));
        assertEquals(80, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 170.91, 172.48, 169.57, 172.09));
        assertEquals(60, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 171.8, 173.31, 170.27, 173.21));
        assertEquals(100, indicator.getValue(), 0.0001);

    }

}
