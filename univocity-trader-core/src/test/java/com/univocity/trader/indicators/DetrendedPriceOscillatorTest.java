package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DetrendedPriceOscillatorTest {

    private static final double[] values = {22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29,
            22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83, 23.95, 23.63, 23.82, 23.87, 23.65, 23.19, 23.10,
            23.33, 22.68, 23.10, 22.40, 22.17, 22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29,
            22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83, 23.95, 23.63, 23.82, 23.87, 23.65, 23.19, 23.10,
            23.33, 22.68, 23.10, 22.40, 22.17, 22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29,
            22.15, 22.39, 22.38, 22.61, 23.36, 24.05, 23.75, 23.83, 23.95, 23.63, 23.82, 23.87, 23.65, 23.19, 23.10,
            23.33, 22.68, 23.10, 22.40, 22.17};

    @Test
    public void dpo() {

        int i = 0;

        DetrendedPriceOscillator indicator = new DetrendedPriceOscillator(9, TimeInterval.minutes(1));

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.12, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(-0.028571428571424917, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.18000000000000327, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(0.16666666666666785, indicator.getValue(), 0.0001);
    }

}
