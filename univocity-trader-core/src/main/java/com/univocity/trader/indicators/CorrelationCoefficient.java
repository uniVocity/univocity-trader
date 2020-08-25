package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class CorrelationCoefficient extends SingleValueCalculationIndicator {

    private Variance variance1;
    private Variance variance2;
    private Covariance covariance;

    public CorrelationCoefficient(int length, TimeInterval interval, Indicator indicator1, Indicator indicator2) {
        super(interval);
        variance1 = new Variance(length, interval, c -> indicator1.getValue());
        variance2 = new Variance(length, interval, c -> indicator2.getValue());
        covariance = new Covariance(length, indicator1, indicator2);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (covariance.accumulate(candle)) {
            variance1.accumulate(candle);
            variance2.accumulate(candle);
            double multipliedSqrt = Math.sqrt(variance1.getValue() * variance2.getValue());
            return covariance.getValue() / multipliedSqrt;
        }
        return 0;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{variance1, variance2, covariance};
    }

}
