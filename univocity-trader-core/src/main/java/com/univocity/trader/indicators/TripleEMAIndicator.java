package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class TripleEMAIndicator extends SingleValueCalculationIndicator {

    private final ExponentialMovingAverage ema;
    private final ExponentialMovingAverage emaEma;
    private final ExponentialMovingAverage emaEmaEma;

    public TripleEMAIndicator(TimeInterval interval) {
        this(5, interval);
    }

    public TripleEMAIndicator(int length, TimeInterval interval) {
        super(interval, null);
        this.ema = new ExponentialMovingAverage(length, interval);
        this.emaEma = new ExponentialMovingAverage(length, interval, c -> ema.getValue());
        this.emaEmaEma = new ExponentialMovingAverage(length, interval, c -> emaEma.getValue());
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        this.ema.accumulate(candle);
        this.emaEma.accumulate(candle);
        this.emaEmaEma.accumulate(candle);
        return 3 * (ema.getValue() - emaEma.getValue()) + emaEmaEma.getValue();
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{ema, emaEma, emaEmaEma};
    }
}
