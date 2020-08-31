package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class BollingerBandWidthTest {

    private double values[] = {10, 12, 15, 14, 17, 20, 21, 20, 20, 19, 20, 17, 12, 12, 9, 8, 9, 10, 9, 10};

    @Test
    public void bollingerBandWidth() {

        int i = 0;
        TimeInterval interval = TimeInterval.MINUTE;

        BollingerBandWidth indicator = new BollingerBandWidth(5, 2, interval);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(36.3636, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(66.6423, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
//        assertEquals(60.2443, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(71.0767, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(69.9394, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(62.7043, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(56.0178, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(27.683, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(12.6491, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(12.6491, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(24.2956, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(68.3332, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(85.1469, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(112.8481, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(108.1682, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(66.9328, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(56.5194, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(28.1091, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(32.5362, indicator.getValue(), 0.0001);
    }

}
