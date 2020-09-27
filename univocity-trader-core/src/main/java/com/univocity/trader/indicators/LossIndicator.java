package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class LossIndicator extends SingleValueCalculationIndicator {

    private double prev = -1;

    public LossIndicator(TimeInterval interval) {
        this(interval, null);
    }

    public LossIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, valueGetter == null ? c -> c.close : valueGetter);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (prev == -1) {
            prev = value;
            return 0;
        }
        double out = 0;
        if (value < prev) {
            out = prev - value;
        }
        prev = value;
        return out;

    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }
}
