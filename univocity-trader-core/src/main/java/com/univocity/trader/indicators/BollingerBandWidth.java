package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.*;

public class BollingerBandWidth extends BollingerBand {

    public BollingerBandWidth(TimeInterval interval) {
        super(interval);
    }

    public BollingerBandWidth(int length, TimeInterval interval) {
        super(length, interval);
    }

    public BollingerBandWidth(int length, double multiplier, TimeInterval interval) {
        super(length, multiplier, interval);
    }

    public BollingerBandWidth(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
    }

    public BollingerBandWidth(int length, double multiplier, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, multiplier, interval, valueGetter);
    }

    @Override
    public double getValue() {
        return ((getUpperBand() - getLowerBand()) / getMiddleBand()) * 100.0;
    }

}
