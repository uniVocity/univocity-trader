package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

public class IchimokuChikouSpanIndicator extends SingleValueCalculationIndicator {

    private final int timeDelay;
    private CircularList list;

    public IchimokuChikouSpanIndicator(TimeInterval interval) {
        this(26, interval);
    }

    public IchimokuChikouSpanIndicator(int timeDelay, TimeInterval interval) {
        super(interval);
        this.timeDelay = timeDelay;
        this.list = new CircularList(timeDelay);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if(list.size() < timeDelay) {
            return Double.NaN;
        }
        list.accumulate(candle.close, updating);
        return list.get(0);
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
