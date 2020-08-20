package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class VarianceIndicator extends MultiValueIndicator {

    private double value;
    private final MovingAverage ma;

    private CircularList closeValues;

    public VarianceIndicator(TimeInterval interval) {
        this(4, interval);
    }

    public VarianceIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public VarianceIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, null);
        this.ma = new MovingAverage(length, interval);
        this.closeValues = new CircularList(length);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        if (ma.accumulate(candle)) {

            closeValues.accumulate(candle.close, updating);

            final double average = ma.getValue();

            final int count = this.values.size();
            int from = values.getStartingIndex();
            int c = count;

            double variance = 0;
            while (c-- > 0) {
                variance += Math.pow(closeValues.get(from) - average, 2);
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

    @Override
    protected Indicator[] children() {
        return new Indicator[]{ma};
    }
}
