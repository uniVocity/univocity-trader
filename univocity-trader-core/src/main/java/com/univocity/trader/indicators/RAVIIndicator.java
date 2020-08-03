package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class RAVIIndicator extends SingleValueIndicator {

    private MovingAverage shortSma;
    private MovingAverage longSma;

    private double value;

    public RAVIIndicator(int shortSmaBarCount, int longSmaBarCount, TimeInterval timeInterval) {
        super(timeInterval, null);
        shortSma = new MovingAverage(shortSmaBarCount, timeInterval);
        longSma = new MovingAverage(longSmaBarCount, timeInterval);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        shortSma.accumulate(candle);
        double shortMA = shortSma.getValue();

        longSma.accumulate(candle);
        double longMA = longSma.getValue();

        this.value = ((shortMA - longMA) / longMA) * 100;

        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
