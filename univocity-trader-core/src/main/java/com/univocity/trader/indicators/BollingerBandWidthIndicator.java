package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class BollingerBandWidthIndicator extends SingleValueCalculationIndicator {

    private final BollingerBand bb;

    public BollingerBandWidthIndicator(BollingerBand bb, TimeInterval interval) {
        super(interval);
        this.bb = bb;
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if(bb.accumulate(candle)) {
            return ((bb.getUpperBand() - bb.getLowerBand()) / bb.getMiddleBand()) * 100;
        }
        return 0;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
