package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class ThreeBlackCrowsIndicator extends MultiValueIndicator {

    private double value;

    private final LowerShadow lowerShadowInd;
    private final MovingAverage averageLowerShadowInd;

    private final CircularList lowerShadowList;
    private final CircularList averageLowerShadowList;
    private final CircularList openList;
    private final CircularList closeList;
    private final CircularList highList;

    private final double factor;
    private int whiteCandleIndex = -1;

    public ThreeBlackCrowsIndicator(double factor, TimeInterval interval) {
        this(14, factor, interval);
    }

    public ThreeBlackCrowsIndicator(int length, double factor, TimeInterval interval) {
        this(length, factor, interval, null);
    }

    public ThreeBlackCrowsIndicator(int length, double factor, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.lowerShadowInd = new LowerShadow(interval);
        this.averageLowerShadowInd = new MovingAverage(length, interval, c -> lowerShadowInd.getValue());

        this.lowerShadowList = new CircularList(length);
        this.averageLowerShadowList = new CircularList(length);
        this.openList = new CircularList(length);
        this.closeList = new CircularList(length);
        this.highList = new CircularList(length);

        this.factor = factor;
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        boolean toReturn = false;

        if (lowerShadowInd.accumulate(candle)) {
            averageLowerShadowInd.accumulate(candle);

            averageLowerShadowList.accumulate(averageLowerShadowInd.getValue(), updating);
            openList.accumulate(candle.open, updating);
            closeList.accumulate(candle.close, updating);
            highList.accumulate(candle.high, updating);

            toReturn = true;

            int count = this.values.size();
            if (count < 3) {
                this.value = 0;
                return toReturn;
            }
            whiteCandleIndex = count - 3;

            System.out.println(closeList.get(whiteCandleIndex) > openList.get(whiteCandleIndex));
            System.out.println(isBlackCrow(count - 2) && isBlackCrow(count - 1));
            System.out.println(isBlackCrow(count));
            System.out.println(" _________________ ");

            this.value = closeList.get(whiteCandleIndex) > openList.get(whiteCandleIndex)
                    && isBlackCrow(count - 2) && isBlackCrow(count - 1)
                    && isBlackCrow(count) ? 1 : 0;
        }
        return toReturn;
    }

    private boolean hasVeryShortLowerShadow(int index) {
        double currentLowerShadow = lowerShadowList.get(index);
        double averageLowerShadow = averageLowerShadowList.get(whiteCandleIndex);
        return currentLowerShadow < (averageLowerShadow * factor);
    }

    private boolean isDeclining(int index) {

        if (values.size() <= 1) {
            return false;
        }

        final double prevOpenPrice = openList.get(index - 1);
        final double prevClosePrice = closeList.get(index - 1);
        final double currOpenPrice = openList.last();
        final double currClosePrice = closeList.last();

        return currOpenPrice < prevOpenPrice && currOpenPrice > prevClosePrice
                && currClosePrice < prevClosePrice;
    }

    private boolean isBlackCrow(int index) {

        if (values.size() <= 1) {
            return false;
        }

        if (openList.last() > closeList.last()) {
            if (closeList.get(index - 1) > openList.get(index - 1)) {
                return hasVeryShortLowerShadow(index) && openList.last() < highList.last();
            } else {
                return hasVeryShortLowerShadow(index) && isDeclining(index);
            }
        }
        return false;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{lowerShadowInd, averageLowerShadowInd};
    }

    @Override
    public double getValue() {
        return super.getValue();
    }

}
