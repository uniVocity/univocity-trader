package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AccelerationDecelerationIndicatorTest {

    private HighLow[] values = {new HighLow(16, 8),
            new HighLow(12, 6),
            new HighLow(18, 14),
            new HighLow(10, 6),
            new HighLow(8, 4)};

    @Test
    public void withSma2AndSma3() {

        int i = 0;

        AccelerationDecelerationIndicator indicator =
                new AccelerationDecelerationIndicator(2, 3, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0.08333333333, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0.41666666666, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(-2, indicator.getValue(), 0.0000001);

    }

    @Test
    public void withSma1AndSma2() {

        int i = 0;

        AccelerationDecelerationIndicator indicator =
                new AccelerationDecelerationIndicator(1, 2, TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

    }

    @Test
    public void withSmaDefault() {

        int i = 0;

        AccelerationDecelerationIndicator indicator =
                new AccelerationDecelerationIndicator(TimeInterval.MINUTE);

        indicator.accumulate(CandleHelper.newCandle(i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

        indicator.accumulate(CandleHelper.newCandle(++i, 0, 0, values[i].high, values[i].low));
        assertEquals(0, indicator.getValue(), 0.0000001);

    }

    class HighLow {

        double high;
        double low;

        public HighLow(double high, double low) {
            this.high = high;
            this.low = low;
        }

    }


}
