package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class ThreeWhiteSoldiersIndicator extends MultiValueIndicator {

    private double value;

    private final UpperShadow upperShadowInd;
    private final MovingAverage averageUpperShadowInd;

    private final CircularList upperShadowList;
    private final CircularList averageUpperShadowList;
    private final CircularList openList;
    private final CircularList closeList;
    private final CircularList highList;

    private int blackCandleIndex = -1;

    private final double factor;

    public ThreeWhiteSoldiersIndicator(double factor, TimeInterval interval) {
        this(3, factor, interval);
    }

    public ThreeWhiteSoldiersIndicator(int length, double factor, TimeInterval interval) {
        this(length, factor, interval, null);
    }

    public ThreeWhiteSoldiersIndicator(int length, double factor, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.upperShadowInd = new UpperShadow(interval);
        this.averageUpperShadowInd = new MovingAverage(length, interval, c -> upperShadowInd.getValue());

        this.upperShadowList = new CircularList(length);
        this.averageUpperShadowList = new CircularList(length);
        this.openList = new CircularList(length);
        this.closeList = new CircularList(length);
        this.highList = new CircularList(length);

        this.factor = factor;
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        boolean toReturn = false;

        if (upperShadowInd.accumulate(candle)) {
            averageUpperShadowInd.accumulate(candle);

            averageUpperShadowList.accumulate(averageUpperShadowInd.getValue(), updating);
            openList.accumulate(candle.open, updating);
            closeList.accumulate(candle.close, updating);
            highList.accumulate(candle.high, updating);

            toReturn = true;

            int count = this.values.size();
            if (count < 3) {
                this.value = 0;
                return toReturn;
            }
            blackCandleIndex = count - 3;

            this.value = closeList.get(blackCandleIndex) < openList.get(blackCandleIndex)
                    && isWhiteSoldier(count - 2) && isWhiteSoldier(count - 1)
                    && isWhiteSoldier(count) ? 1 : 0;
        }
        return toReturn;
    }

    private boolean hasVeryShortUpperShadow(int index) {
        double currentUpperShadow = upperShadowList.get(index);
        double averageUpperShadow = averageUpperShadowList.get(blackCandleIndex);
        return currentUpperShadow < (averageUpperShadow * factor);
    }

    private boolean isGrowing(int index) {
        if (values.size() <= 1) {
            return false;
        }

        final double prevOpenPrice = openList.get(index - 1);
        final double prevClosePrice = closeList.get(index - 1);
        final double currOpenPrice = openList.last();
        final double currClosePrice = closeList.last();

        return currOpenPrice > prevOpenPrice && currOpenPrice < prevClosePrice
                && currClosePrice > prevClosePrice;
    }

    private boolean isWhiteSoldier(int index) {
        if (values.size() <= 1) {
            return false;
        }

        if (openList.last() < closeList.last()) {
            if (closeList.get(index - 1) < openList.get(index - 1)) {
                return hasVeryShortUpperShadow(index) && openList.last() > highList.last();
            } else {
                return hasVeryShortUpperShadow(index) && isGrowing(index);
            }
        }
        return false;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[] {upperShadowInd, averageUpperShadowInd};
    }

    @Override
    public double getValue() {
        return value;
    }
}
