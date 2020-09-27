package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Assert;
import org.junit.Test;

public class NegativeVolumeIndexTest {

    @Test
    public void nvi() {

        int i = 0;
        double ZERO = 0;

        NegativeVolumeIndex indicator = new NegativeVolumeIndex(TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1355.69, ZERO, ZERO, 2739.55));
        Assert.assertEquals(1000, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1325.51, ZERO, ZERO, 3119.46));
        Assert.assertEquals(1000, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1335.02, ZERO, ZERO, 3466.88));
        Assert.assertEquals(1000, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1313.72, ZERO, ZERO, 2577.12));
        Assert.assertEquals(984.0452, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1319.99, ZERO, ZERO, 2480.45));
        Assert.assertEquals(988.7417, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1331.85, ZERO, ZERO, 2329.79));
        Assert.assertEquals(997.6255, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1329.04, ZERO, ZERO, 2793.07));
        Assert.assertEquals(997.6255, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1362.16, ZERO, ZERO, 3378.78));
        Assert.assertEquals(997.6255, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1365.51, ZERO, ZERO, 2417.59));
        Assert.assertEquals(1000.079, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, ZERO, 1374.02, ZERO, ZERO, 1442.81));
        Assert.assertEquals(1006.3116, indicator.getValue(), 0.0001);

    }

}
