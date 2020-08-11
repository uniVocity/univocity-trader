package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class HullMovingAverageIndicator extends SingleValueIndicator {

    private double value;
    private WeightedMovingAverage halfWma;
    private WeightedMovingAverage origWma;
    private WeightedMovingAverage sqrtWma;

    public HullMovingAverageIndicator(TimeInterval interval) {
        this(9, interval);
    }

    public HullMovingAverageIndicator(int length, TimeInterval interval) {
        this(length, interval, null);
    }

    public HullMovingAverageIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, valueGetter);
        halfWma = new WeightedMovingAverage(length / 2, interval);
        origWma = new WeightedMovingAverage(length, interval);
        sqrtWma = new WeightedMovingAverage(Double.valueOf(Math.sqrt(length)).intValue(), interval, c -> (halfWma.getValue() * 2) - origWma.getValue());
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        if(halfWma.accumulate(candle)) {
            origWma.accumulate(candle);
            sqrtWma.accumulate(candle);
            this.value = sqrtWma.getValue();
        }
        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{halfWma, origWma, sqrtWma};
    }

}