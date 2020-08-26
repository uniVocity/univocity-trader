package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

public class StandardErrorIndicator extends SingleValueCalculationIndicator {

    private CircularList list;
    private StandardDeviation sdev;

    public StandardErrorIndicator(TimeInterval interval) {
        this(5, interval);
    }

    public StandardErrorIndicator(int length, TimeInterval interval) {
        super(interval);
        this.sdev = new StandardDeviation(length, interval);
        this.list = new CircularList(length);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (sdev.accumulate(candle)) {
            list.accumulate(value, updating);
            return sdev.getValue() / Math.sqrt(list.size());
        }
        return 0;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[0];
    }

}
