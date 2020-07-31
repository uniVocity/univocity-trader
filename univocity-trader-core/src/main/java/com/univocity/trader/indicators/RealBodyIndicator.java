package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class RealBodyIndicator extends SingleValueIndicator {

    private double value;

    public RealBodyIndicator(TimeInterval timeInterval) {
        super(timeInterval, null);
    }

    /**
     * Provides the difference between the open price and the close price
     * of a bar. I.e.: close price - open price
     */
    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        this.value = candle.close - candle.open;
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
