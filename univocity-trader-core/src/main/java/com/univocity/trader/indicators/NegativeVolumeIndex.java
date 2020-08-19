package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class NegativeVolumeIndex extends SingleValueCalculationIndicator {

    private Candle last;

    public NegativeVolumeIndex(TimeInterval interval) {
        super(interval, null);
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        double toReturn;
        if (last == null) {
            toReturn = 1000;
        } else {
            if(candle.volume < last.volume) {
                double currentPrice = candle.close;
                double previousPrice = last.close;
                double priceChangeRatio = (currentPrice - previousPrice) / previousPrice;
                toReturn = previousValue + (priceChangeRatio * previousValue);
            }else {
                toReturn = previousValue;
            }
        }
        last = candle;
        return toReturn;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
