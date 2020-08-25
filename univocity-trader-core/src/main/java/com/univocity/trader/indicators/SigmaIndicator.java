package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class SigmaIndicator extends SingleValueCalculationIndicator {

    private final MovingAverage mean;
    private final StandardDeviation sd;

    public SigmaIndicator(int length, TimeInterval interval) {
        super(interval);
        mean = new MovingAverage(length, interval);
        sd = new StandardDeviation(length, interval);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (mean.accumulate(candle)) {
            sd.accumulate(candle);
            return (candle.close - mean.getValue()) / sd.getValue();
        }
        return 0;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{mean, sd};
    }

}
