package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

import static com.univocity.trader.indicators.base.TimeInterval.MINUTE;

public class UlcerIndexIndicator extends MultiValueIndicator {

    private double value;

    private HighestValueIndicator highestValueInd;
    private double length;

    public UlcerIndexIndicator(TimeInterval interval) {
        this(14, interval);
    }

    public UlcerIndexIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public UlcerIndexIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.length = length;
        highestValueInd = new HighestValueIndicator(length, interval, c -> c.close);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        final int index = Double.valueOf(candle.openTime / MINUTE.ms).intValue();

        final int startIndex = Double.valueOf(Math.max(0, index - length + 1)).intValue();
        final int numberOfObservations = index - startIndex + 1;
        double squaredAverage = 0;

        if (highestValueInd.accumulate(candle)) {

            for (int i = startIndex; i <= index; i++) {
                double currentValue = values.get(i);
                double highestValue = highestValueInd.getValue();
                double percentageDrawdown = ((currentValue - highestValue) / highestValue) * 100;
                squaredAverage = squaredAverage + Math.pow(percentageDrawdown, 2);
            }

            this.value = Math.sqrt(squaredAverage / numberOfObservations);

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
        return new Indicator[]{highestValueInd};
    }

}
