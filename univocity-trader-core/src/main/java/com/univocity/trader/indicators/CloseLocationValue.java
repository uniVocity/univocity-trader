package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class CloseLocationValue extends SingleValueCalculationIndicator {

    public CloseLocationValue(TimeInterval interval) {
        super(interval, null);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        final double low = candle.low;
        final double high = candle.high;
        final double close = candle.close;
        final double diffHighLow = high - low;
        return diffHighLow == 0 ? 0 : ((close - low) - (high - close)) / diffHighLow;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
