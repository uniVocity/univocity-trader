package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class MedianPrice extends SingleValueIndicator {

    private double value;

    public MedianPrice(TimeInterval interval) {
        super(interval, null);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        this.value = (candle.high + candle.low) / 2;
        return true;
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
