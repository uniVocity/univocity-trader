package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import org.junit.Test;

import static com.univocity.trader.indicators.base.TimeInterval.minutes;
import static org.junit.Assert.assertEquals;

public class UlcerIndexIndicatorTest {

    @Test
    public void ulcer() {
        int i = 0;

        UlcerIndexIndicator indicator = new UlcerIndexIndicator(minutes(1));

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 194.75, 0, 0));
        assertEquals(0, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.00, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.10, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 194.46, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 190.60, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 188.86, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 185.47, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 184.46, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 182.31, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 185.22, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 184.00, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 182.87, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 187.45, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 194.51, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 191.63, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 190.02, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 189.53, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 190.27, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 193.13, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.55, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.84, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.15, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 194.35, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 193.62, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 197.68, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 197.91, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 199.08, 0, 0));
        assertEquals(1.3047, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 199.03, 0, 0));


        indicator.accumulate(CandleHelper.newCandle(i++, 0, 198.42, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 199.29, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 199.01, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 198.29, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 198.40, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 200.84, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 201.22, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 200.50, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 198.65, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 197.25, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.70, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 197.77, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.69, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 194.87, 0, 0));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 195.08, 0, 0));


    }

}
