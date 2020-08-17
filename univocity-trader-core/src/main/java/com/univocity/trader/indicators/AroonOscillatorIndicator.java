package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class AroonOscillatorIndicator extends SingleValueIndicator {

    private final AroonDown aroonDown;
    private final AroonUp aroonUp;

    public AroonOscillatorIndicator(TimeInterval interval) {
        this(25, interval);
    }

    public AroonOscillatorIndicator(int length, TimeInterval interval) {
        super(interval, null);
        aroonDown = new AroonDown(length, interval, c -> c.low);
        aroonUp = new AroonUp(length, interval, c -> c.high);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        aroonUp.accumulate(candle);
        aroonDown.accumulate(candle);
        return true;
    }

    @Override
    public double getValue() {
        return aroonUp.getValue() - aroonDown.getValue();
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{aroonDown, aroonUp};
    }

}