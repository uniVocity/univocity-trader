package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class PercentBIndicator extends SingleValueCalculationIndicator {

    private final BollingerBand bb;

    public PercentBIndicator(TimeInterval interval) {
        this(5, 2, interval);
    }

    public PercentBIndicator(int length, double multiplier, TimeInterval interval) {
        super(interval);
        BollingerBand bbm = new BollingerBand(length, interval, c -> new MovingAverage(length, interval).getValue());
        this.bb = new BollingerBand(length, multiplier, interval, c -> bbm.getValue());
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if(this.bb.accumulate(candle)) {
            return (candle.close - bb.getLowerBand()) / ( bb.getUpperBand() - bb.getLowerBand());
        }
        return 0;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{bb};
    }
}
