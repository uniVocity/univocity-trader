package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class LowerShadowIndicator extends SingleValueCalculationIndicator {

    public LowerShadowIndicator(TimeInterval interval) {
        super(interval);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        return candle.close > candle.open ? candle.open - candle.low : candle.close - candle.low;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[0];
    }

}
