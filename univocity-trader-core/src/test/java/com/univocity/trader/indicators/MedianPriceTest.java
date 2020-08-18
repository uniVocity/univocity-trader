package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MedianPriceTest {

    private HighLow[] values = {new HighLow(16, 8),
            new HighLow(12, 6),
            new HighLow(18, 14),
            new HighLow(10, 6),
            new HighLow(32, 6),
            new HighLow(2, 2),
            new HighLow(0, 0),
            new HighLow(8, 1),
            new HighLow(83, 32),
            new HighLow(9, 3)};

    @Test
    public void median() {

        MedianPrice indicator = new MedianPrice(TimeInterval.MINUTE);

        double result;
        for (int i = 0; i < 10; i++) {
            result = (values[i].high + values[i].low) / 2;
            indicator.accumulate(CandleHelper.newCandle(i, 0, 0, values[i].high, values[i].low));
            assertEquals(result, indicator.getValue(), 0.0000001);
        }

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
