package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class TRIndicator extends SingleValueCalculationIndicator {

    private Candle last;

    public TRIndicator(TimeInterval interval) {
        super(interval, null);
        last = null;
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {

        double ts = candle.high - candle.low;
        double ys = last == null ? 0 : candle.high - last.close;
        double yst = last == null ? 0 : last.close - candle.low;

        last = candle;

        return Math.max(Math.abs(yst), Math.max(Math.abs(ts), Math.abs(ys)));
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
