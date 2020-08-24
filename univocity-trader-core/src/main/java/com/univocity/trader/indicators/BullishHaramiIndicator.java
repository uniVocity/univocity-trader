package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class BullishHaramiIndicator extends SingleValueIndicator {

    private double value;
    private Candle prev;

    public BullishHaramiIndicator(TimeInterval timeInterval) {
        super(timeInterval, null);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        boolean toReturn = false;
        this.value = 0;

        if (prev != null && !prev.isClosePositive() && candle.isClosePositive()) {

            final double prevOpenPrice = prev.open;
            final double prevClosePrice = prev.close;
            final double currOpenPrice = candle.open;
            final double currClosePrice = candle.close;

            this.value = (currOpenPrice < prevOpenPrice && currOpenPrice > prevClosePrice
                    && currClosePrice < prevOpenPrice && currClosePrice > prevClosePrice) ? 1 : 0;

            toReturn = true;

        }

        prev = candle;

        return toReturn;
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
