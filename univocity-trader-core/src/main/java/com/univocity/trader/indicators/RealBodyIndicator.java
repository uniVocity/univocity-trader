package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class RealBodyIndicator extends MultiValueIndicator {

    private double value;

    public RealBodyIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
    }

    /**
     * Provides the difference between the open price and the close price
     *  of a bar. I.e.: close price - open price
     */
    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
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
