package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import org.junit.Test;

import static com.univocity.trader.indicators.base.TimeInterval.minutes;
import static org.junit.Assert.assertEquals;

public class WeightedMovingAverageTest {

    @Test
    public void calculate() {

        int i = 0;

        final double[] values = {1d, 2d, 3d, 4d, 5d, 6d};
        WeightedMovingAverage indicator = new WeightedMovingAverage(3, minutes(1));

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(1.6667, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(2.3333, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
        assertEquals(3.3333, indicator.getValue(), 0.0001);

    }

}
