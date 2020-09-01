package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.utils.CircularList;

import static com.univocity.trader.indicators.Signal.*;

public class ThreeWhiteSoldiers extends MovingAverage {

    private final CircularList averageUpperShadowList;
    private final double factor;

    private Candle blackCandle;
    private Candle c1;
    private Candle c2;

    public ThreeWhiteSoldiers(TimeInterval interval) {
        this(3, 0.3, interval);
    }

    public ThreeWhiteSoldiers(double factor, TimeInterval interval) {
        this(3, factor, interval);
    }

    public ThreeWhiteSoldiers(int length, TimeInterval interval) {
        this(length, 0.3, interval);
    }

    public ThreeWhiteSoldiers(int length, double factor, TimeInterval interval) {
        super(length, interval, c -> c.close > c.open ? c.high - c.close : c.high - c.open);
        this.averageUpperShadowList = new CircularList(4);
        this.factor = factor;
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
        if (super.calculateIndicatorValue(candle, value, updating)) {
            averageUpperShadowList.add(super.getValue());
            return true;
        }
        return false;
    }

    private boolean hasVerShortUpperShadow(Candle candle) {
        double currentUpperShadow = valueGetter.applyAsDouble(candle);
        double averageUpperShadow = averageUpperShadowList.first();
        return currentUpperShadow < (averageUpperShadow * factor);
    }

    private boolean isGrowing(Candle candle, Candle prev) {
        return candle.open > prev.open && candle.open < prev.close && candle.close > prev.close;
    }

    private boolean isWhiteSoldier(Candle candle, Candle prev) {
        if (candle.isGreen()) {
            if (prev.isRed()) {
                return hasVerShortUpperShadow(candle) && candle.open > prev.low;
            } else {
                return hasVerShortUpperShadow(candle) && isGrowing(candle, prev);
            }
        }
        return false;
    }

    @Override
    public double getValue() {
        return getSignal(null) == BUY ? 1.0 : 0.0;
    }

    @Override
    protected Signal calculateSignal(Candle candle) {
        Signal signal;
        if (blackCandle == null) {
            signal = Signal.NEUTRAL;
        } else {
            signal = blackCandle.isRed() && isWhiteSoldier(c1, blackCandle) && isWhiteSoldier(c2, c1) && isWhiteSoldier(candle, c2) ? BUY : Signal.NEUTRAL;
        }
        blackCandle = c1;
        c1 = c2;
        c2 = candle;
        return signal;
    }

    @Override
    public String signalDescription() {
        return getSignal(null) == BUY ? "3 white soldiers" : "";
    }
}
