package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class MassIndexIndicator extends MultiValueIndicator {

    private double value;
    private final ExponentialMovingAverage singleEma;
    private final ExponentialMovingAverage doubleEma;

    public MassIndexIndicator(int emaBarCount, TimeInterval interval) {
        this(emaBarCount, 8, interval);
    }

    public MassIndexIndicator(int emaBarCount, int length, TimeInterval interval) {
        this(emaBarCount, length, interval, c -> c.close);
    }

    public MassIndexIndicator(int emaBarCount, int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        singleEma = new ExponentialMovingAverage(emaBarCount, interval, candle -> candle.high - candle.low);
        doubleEma = new ExponentialMovingAverage(emaBarCount, interval, valueGetter);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        singleEma.accumulate(candle);
        doubleEma.accumulate(singleEma.getValue());

        final int size = values.size();
        int remaining = size;
        double massIndex = 0;

        while (remaining-- > 0) {
            double emaRatio = singleEma.getValue() / doubleEma.getValue();
            massIndex = massIndex + emaRatio;
        }

        this.value = massIndex;

        return false;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{singleEma, doubleEma};
    }


}
