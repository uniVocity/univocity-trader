package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class AbstractIchimokuLineIndicator extends SingleValueCalculationIndicator {

    private HighestValueIndicator high;
    private LowestValueIndicator low;

    public AbstractIchimokuLineIndicator(int length, TimeInterval interval) {
        super(interval);
        this.high = new HighestValueIndicator(length, interval, c -> c.high);
        this.low = new LowestValueIndicator(length, interval, c -> c.low);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (high.accumulate(candle)) {
            low.accumulate(candle);
            return (high.getValue() + low.getValue()) / 2;
        }
        return Double.NaN;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
