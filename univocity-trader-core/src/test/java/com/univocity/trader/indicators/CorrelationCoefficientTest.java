package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.candles.CandleHelper;
import com.univocity.trader.indicators.base.TimeInterval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CorrelationCoefficientTest {

    private CloseVolume[] candles = {new CloseVolume(6, 100),
                                    new CloseVolume(7, 105),
                                    new CloseVolume(9, 130),
                                    new CloseVolume(12, 160),
                                    new CloseVolume(11, 150),
                                    new CloseVolume(10, 130),
                                    new CloseVolume(11, 95),
                                    new CloseVolume(13, 120),
                                    new CloseVolume(15, 180),
                                    new CloseVolume(12, 160),
                                    new CloseVolume(8, 150),
                                    new CloseVolume(4, 200),
                                    new CloseVolume(3, 150),
                                    new CloseVolume(4, 85),
                                    new CloseVolume(3, 70),
                                    new CloseVolume(5, 90),
                                    new CloseVolume(8, 100),
                                    new CloseVolume(9, 95),
                                    new CloseVolume(11, 110),
                                    new CloseVolume(10, 95)};

    @Test
    public void correlationCoefficient() {

        int i = 0;

        TimeInterval interval = TimeInterval.MINUTE;

        Volume volume = new Volume(2, interval);
        CorrelationCoefficient indicator = new CorrelationCoefficient(5, new FunctionIndicator(interval, c -> c.close), volume);

        indicator.accumulate(candles[i].getCandle(i++));
        assertTrue(Double.isNaN(indicator.getValue()));

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(1, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.8773, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.9073, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.9219, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.9205, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.4565, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(-0.4622, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.05747, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.1442, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(-0.1263, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(-0.5345, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(-0.7275, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.1676, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.2506, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(-0.2938, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(-0.3586, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.1713, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.9841, indicator.getValue(), 0.0001);

        indicator.accumulate(candles[i].getCandle(i++));
        assertEquals(0.9799, indicator.getValue(), 0.0001);

    }

    class CloseVolume {

        double close;
        double volume;

        public CloseVolume(double close, double volume) {
            this.close = close;
            this.volume = volume;
        }

        public Candle getCandle(int i) {
            return CandleHelper.newCandle(i, 0, close, 0, 0, volume);
        }

    }

}
