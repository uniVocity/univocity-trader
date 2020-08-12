package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class VolumeIndicator extends MultiValueIndicator {

    public VolumeIndicator(TimeInterval interval) {
        this(3, interval);
    }

    public VolumeIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.volume);
    }

    public VolumeIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
               return true;
    }

    @Override
    public double getValue() {
        return values.sum();
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
