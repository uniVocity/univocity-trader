package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.*;

public class TripleExponentialMovingAverage extends SingleValueCalculationIndicator {

    private final ExponentialMovingAverage ema;
    private final ExponentialMovingAverage emaEma;
    private final ExponentialMovingAverage emaEmaEma;

    public TripleExponentialMovingAverage(TimeInterval interval) {
        this(5, interval, null);
    }

    public TripleExponentialMovingAverage(int length, TimeInterval interval) {
        this(length, interval, null);
    }

    public TripleExponentialMovingAverage(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, null);
        valueGetter = valueGetter == null ? c->c.close : valueGetter;
        this.ema = new ExponentialMovingAverage(length, interval, valueGetter);
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
