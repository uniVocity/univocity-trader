package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class LinearlyWeightedMovingAverageIndicator extends MultiValueIndicator {

    private double value;
    private int length;

    public LinearlyWeightedMovingAverageIndicator(TimeInterval interval) {
        this(5, interval);
    }

    public LinearlyWeightedMovingAverageIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public LinearlyWeightedMovingAverageIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.length = length;
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        double sum = 0;
        double denominator = 0;

        int count = 0;
        int index = this.values.size() - 1;

        if ((index + 1) < length) {
            this.value = 0;
            return true;
        }

        int startIndex = (index - length) + 1;
        for (int i = startIndex; i <= index; i++) {
            count++;
            denominator = denominator + count;
            sum = sum + (this.values.values[i] * count);
        }

        this.value = sum / denominator;

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