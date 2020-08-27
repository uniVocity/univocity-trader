package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;

import java.util.function.ToDoubleFunction;

public class ThreeBlackCrowsIndicator extends MultiValueIndicator {

    private double value;

    private final LowerShadow lowerShadowInd;
    private final MovingAverage averageLowerShadowInd;
    private final double factor;
    private int whiteCandleIndex = -1;

    public ThreeBlackCrowsIndicator(double factor, TimeInterval interval) {
        this(14, factor, interval);
    }

    public ThreeBlackCrowsIndicator(int length, double factor, TimeInterval interval) {
        this(length, factor, interval, c -> c.close);
    }

    public ThreeBlackCrowsIndicator(int length, double factor, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        lowerShadowInd = new LowerShadow(interval);
        averageLowerShadowInd = new MovingAverage(length, interval, c -> lowerShadowInd.getValue());
        this.factor = factor;
    }

    private boolean hasVeryShortLowerShadow(Candle candle) {
        double currentLowerShadow = lowerShadowInd.getValue();
        // We use the white candle index to remove to bias of the previous crows
        double averageLowerShadow = averageLowerShadowInd.getValue(whiteCandleIndex);

        return currentLowerShadow.isLessThan(averageLowerShadow.multipliedBy(factor));
    }

}
