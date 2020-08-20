package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class Variance extends MovingAverage {

    private double value;

    public Variance(TimeInterval interval) {
        this(4, interval);
    }

    public Variance(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public Variance(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
        if(super.calculateIndicatorValue(candle, value, updating)){
            final double average = super.getValue();

            final int count = this.values.size();
            int from = values.getStartingIndex();
            int c = count;

            double variance = 0;
            while (c-- > 0) {
                double v = values.get(from) - average;
                variance += v * v;
                from = (from + 1) % count;
            }

            this.value = variance / count;
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return this.value;
    }
}
