package com.univocity.trader.indicators;

import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChaikinMoneyFlowTest {

    @Test
    public void chaikinMoneyFlow() {

        int i = 0;

        ChaikinMoneyFlow indicator = new ChaikinMoneyFlow(TimeInterval.MINUTE);
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 62.15, 62.34, 61.37, 7849.025));
        assertEquals(0.6082, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 60.81, 62.05, 60.69, 11692.075));
        assertEquals(-0.2484, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 60.45, 62.27, 60.10, 10575.307));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 59.18, 60.79, 58.61, 13059.128));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 59.24, 59.93, 58.71, 20733.508));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 60.20, 61.75, 59.86, 29630.096));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.48, 60.00, 57.97, 17705.294));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.24, 59.00, 58.02, 7259.203));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.69, 59.07, 57.48, 10474.629));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.65, 59.22, 58.30, 5203.714));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.47, 58.75, 57.83, 3422.865));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.02, 58.65, 57.86, 3962.150));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.17, 58.47, 57.91, 4095.905));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.07, 58.25, 57.83, 3766.006));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.13, 58.35, 57.53, 4239.335));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 58.94, 59.86, 58.58, 8039.979));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 59.10, 59.53, 58.30, 6956.717));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 61.92, 62.10, 58.53, 18171.552));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 61.37, 62.16, 59.80, 22225.894));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 61.68, 62.67, 60.93, 14613.509));
        assertEquals(-0.1211, indicator.getValue(), 0.0001);

        indicator.accumulate(CandleHelper.newCandle(i++, 0, 62.09, 62.38, 60.15, 12319.763));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 62.89, 63.73, 62.26, 15007.690));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 63.53, 63.85, 63.00, 8879.667));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 64.01, 66.15, 63.58, 22693.812));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 64.77, 65.34, 64.07, 10191.814));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 65.22, 66.48, 65.20, 10074.152));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 63.28, 65.23, 63.21, 9411.620));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 62.40, 63.40, 61.88, 10391.690));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 61.55, 63.18, 61.11, 8926.512));
        indicator.accumulate(CandleHelper.newCandle(i++, 0, 62.69, 62.70, 61.25, 7459.575));
    }

}
