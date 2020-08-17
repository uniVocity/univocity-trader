package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class AroonUpIndicator extends MultiValueIndicator {

    private double value;
    private double humdred;
    private HighestValueIndicator highestValueIndicator;

    private double lastMax;

    public AroonUpIndicator(TimeInterval interval) {
        this(20, interval);
    }

    public AroonUpIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public AroonUpIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.humdred = 100;
        this.lastMax = 0.0;
        highestValueIndicator = new HighestValueIndicator(length + 1, interval, c -> c.close);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        highestValueIndicator.accumulate(candle);
        if (lastMax != highestValueIndicator.getValue()) {
            lastMax = highestValueIndicator.getValue();
        }

        int length = values.capacity();
        int nbBars = 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.getRecentValue(i) == lastMax) {
                break;
            }
            nbBars++;
        }

        this.value = ((length - nbBars) / length) * humdred;

        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{highestValueIndicator};
    }

}
