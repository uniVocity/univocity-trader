package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class WMAIndicator extends MultiValueIndicator {

    private double value;
    private int length;

    public WMAIndicator(TimeInterval interval) {
        this(3, interval);
    }

    public WMAIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public WMAIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.length = length;
    }


    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        final int size = values.size();
        final int index = values.getStartingIndex() + 1;

        if (size == 1) {
            this.value = values.get(0);
            return true;
        }

        if (index - length < 0) {

//            this.value = 0;

            for (int i = index + 1; i > 0; i--) {
                this.value = this.value + (i * (values.get(i - 1)));
            }

            this.value = this.value / (((index + 1) * (index + 2)) / 2);
            return true;
        }

        for (int i = length; i > 0; i--) {
            this.value = this.value + (i * (candle.close));
        }

        this.value = this.value / ((size * (size + 1)) / 2);

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
