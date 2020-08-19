package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PositiveVolumeIndexTest {

    @Test
    public void positiveVolumeIndex() {

        int i = 0;
        double ZERO = 0;

        PositiveVolumeIndex indicator = new PositiveVolumeIndex(TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1355.69, ZERO, ZERO, 2739.55));
        assertEquals(1000, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1325.51, ZERO, ZERO, 3119.46));
        assertEquals(977.7383, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1335.02, ZERO, ZERO, 3466.88));
        assertEquals(984.7532, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1313.72, ZERO, ZERO, 2577.12));
        assertEquals(984.7532, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1319.99, ZERO, ZERO, 2480.45));
        assertEquals(984.7532, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1331.85, ZERO, ZERO, 2329.79));
        assertEquals(984.7532, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1329.04, ZERO, ZERO, 2793.07));
        assertEquals(982.6755, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1362.16, ZERO, ZERO, 3378.78));
        assertEquals(1007.164, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1365.51, ZERO, ZERO, 2417.59));
        assertEquals(1007.164, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1374.02, ZERO, ZERO, 1442.81));
        assertEquals(1007.164, indicator.getValue(), 0.0001);


    }

}

