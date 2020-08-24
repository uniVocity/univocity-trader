package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class StandardDeviationIndicator extends SingleValueCalculationIndicator {

    private final Variance variance;

    public StandardDeviationIndicator(TimeInterval interval) {
        this(4, interval);
    }

    public StandardDeviationIndicator(int length, TimeInterval interval) {
        super(interval, null);
        variance = new Variance(length, interval);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        this.variance.accumulate(candle);
        return Math.sqrt(this.variance.getValue());
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{variance};
    }

}