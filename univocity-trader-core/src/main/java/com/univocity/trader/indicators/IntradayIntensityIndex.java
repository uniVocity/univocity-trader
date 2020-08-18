package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class IntradayIntensityIndex extends SingleValueCalculationIndicator {

    public IntradayIntensityIndex(TimeInterval timeInterval) {
        super(timeInterval, null);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        return ((candle.close * 2) - (candle.high + candle.low)) / ((candle.high - candle.low) * candle.volume);
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
