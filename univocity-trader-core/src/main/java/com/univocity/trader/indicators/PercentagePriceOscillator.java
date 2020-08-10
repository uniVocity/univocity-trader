package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class PercentagePriceOscillator extends SingleValueIndicator {

    private double value;

    private final ExponentialMovingAverage shortTermEma;
    private final ExponentialMovingAverage longTermEma;

    public PercentagePriceOscillator(int shortBarCount, int longBarCount, TimeInterval interval) {
        this(shortBarCount, longBarCount, interval, c -> c.close);
    }

    public PercentagePriceOscillator(int shortBarCount, int longBarCount, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, null);
        if (shortBarCount > longBarCount) {
            throw new IllegalArgumentException("Long term period count must be greater than short term period count");
        }
        this.shortTermEma = new ExponentialMovingAverage(shortBarCount, interval, valueGetter);
        this.longTermEma = new ExponentialMovingAverage(longBarCount, interval, valueGetter);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        shortTermEma.accumulate(candle);
        double shortEmaValue = shortTermEma.getValue();

        longTermEma.accumulate(candle);
        double longEmaValue = longTermEma.getValue();

        this.value = ((shortEmaValue - longEmaValue) / longEmaValue) * 100;

        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{shortTermEma, longTermEma};
    }

}
