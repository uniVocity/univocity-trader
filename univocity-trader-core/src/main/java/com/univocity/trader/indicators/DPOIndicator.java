package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class DPOIndicator extends SingleValueIndicator {

    private double value;

    private MovingAverage ma;

    public DPOIndicator(TimeInterval interval) {
        this(9, interval);
    }

    public DPOIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public DPOIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, valueGetter);
        ma = new MovingAverage(length, interval, c -> c.close);
    }


    @Override
    protected boolean process(Candle candle, double value, boolean updating) {

        ma.accumulate(candle);

        this.value = candle.close - ma.getValue();
        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{ma};
    }

}
