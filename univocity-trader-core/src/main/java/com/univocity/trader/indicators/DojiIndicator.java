package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class DojiIndicator extends MultiValueIndicator {

    private double value;
    private double bodyFactor;

    private final RealBodyIndicator bodyHeightInd;
    private final MovingAverage averageBodyHeightInd;

    public DojiIndicator(TimeInterval interval) {
        this(0.03, 10, interval);
    }

    public DojiIndicator(double bodyFactor, int length, TimeInterval interval) {
        super(length, interval, null);
        this.bodyHeightInd = new RealBodyIndicator(interval);
        this.averageBodyHeightInd = new MovingAverage(length, interval, c -> Math.abs(bodyHeightInd.getValue()));
        this.bodyFactor = bodyFactor;
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        if (this.bodyHeightInd.accumulate(candle)) {
            this.averageBodyHeightInd.accumulate(candle);

            if (values.size() == 1) {
                this.value = Math.abs(bodyHeightInd.getValue()) == 0 ? 1 : 0;
                return true;
            }

            double averageBodyHeight = averageBodyHeightInd.getValue();
            double currentBodyHeight = Math.abs(bodyHeightInd.getValue());

            this.value = currentBodyHeight < (averageBodyHeight * this.bodyFactor) ? 1 : 0;
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
        return new Indicator[]{bodyHeightInd, averageBodyHeightInd};
    }
}
