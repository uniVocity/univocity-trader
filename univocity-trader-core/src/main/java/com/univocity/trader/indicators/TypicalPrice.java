package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class TypicalPrice extends SingleValueIndicator {

    private double value;

    public TypicalPrice(TimeInterval interval) {
        super(interval, null);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        this.value = (candle.high + candle.low + candle.close) / 3.0;
        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
