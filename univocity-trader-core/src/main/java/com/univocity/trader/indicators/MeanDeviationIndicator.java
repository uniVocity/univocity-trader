package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class MeanDeviationIndicator extends MultiValueIndicator {

    private double value;
    private final MovingAverage sma;

    public MeanDeviationIndicator(TimeInterval interval) {
        this(5, interval);
    }

    public MeanDeviationIndicator(int length, TimeInterval interval) {
        this(length, interval, null);
    }

    public MeanDeviationIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter == null ? c -> c.close : valueGetter);
        this.sma = new MovingAverage(length, interval);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        if (sma.accumulate(candle)) {

            int index = values.size() - 1;

            double absoluteDeviations = 0;

            final double average = sma.getValue();
            final int startIndex = Math.max(0, index - 5 + 1);
            final int nbValues = index - startIndex + 1;

            for (int i = startIndex; i <= index; i++) {
                absoluteDeviations = absoluteDeviations + Math.abs(values.getRecentValue(i + 1) - average);
            }

            this.value = absoluteDeviations / nbValues;

            return true;

        }

        return false;

    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{sma};
    }

}
