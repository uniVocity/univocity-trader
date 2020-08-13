package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class GainIndicator extends MultiValueIndicator {

    private double value;

    public GainIndicator(TimeInterval interval) {
        this(13, interval);
    }

    public GainIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public GainIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
        if(values.size() <= 1) {
            this.value = 0;
            return true;
        }

        double previous = values.getRecentValue(2);

        if(candle.close > previous) {
            this.value = candle.close - previous;
            return true;
        }

        this.value = 0;
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
