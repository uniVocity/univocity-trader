package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntradayIntensityIndexTest {

    @Test
    public void intradayIntensityIndex() {

        int i = 0;

        IntradayIntensityIndex indicator = new IntradayIntensityIndex(TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i++, 0d, 10d, 12d, 8d, 200d));
        assertEquals(0, indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0d, 8d, 10d, 7d, 100d));
        assertEquals((2 * 8d - 10d - 7d) / ((10d - 7d) * 100d), indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0d, 9d, 15d, 6d, 300d));
        assertEquals((2 * 9d - 15d - 6d) / ((15d - 6d) * 300d), indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0d, 20d, 40d, 5d, 50d));
        assertEquals((2 * 20d - 40d - 5d) / ((40d - 5d) * 50d), indicator.getValue(), 0.001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0d, 30d, 30d, 3d, 600d));
        assertEquals((2 * 30d - 30d - 3d) / ((30d - 3d) * 600d), indicator.getValue(), 0.001);

    }

}
