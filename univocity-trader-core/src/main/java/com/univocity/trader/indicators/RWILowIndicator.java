package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class RWILowIndicator extends MultiValueIndicator {

    private double value;
    private AverageTrueRange atrIndicator;

    private CircularList highList;

    public RWILowIndicator(TimeInterval interval) {
        this(20, interval);
    }

    public RWILowIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public RWILowIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        highList = new CircularList(length);
        atrIndicator = new AverageTrueRange(length, interval);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        if(atrIndicator.accumulate(candle)) {
            highList.accumulate(candle.high, updating);

            if(values.size() < values.capacity()) {
                this.value = Double.NaN;
                return true;
            }

            double minRWIL = 0;
            for (int n = 2; n <= values.size(); n++) {
                minRWIL = Math.max(minRWIL, calcRWIHFor(candle, n));
            }

            this.value =  minRWIL;
            return true;

        }

        return false;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{atrIndicator};
    }

    private double calcRWIHFor(final Candle candle, final int n) {
        double low = candle.low;
        double highN = highList.get(values.size() - n);
        double atrN = atrIndicator.getValue();
        double sqrtN = Math.sqrt(n);

        return (highN - low) / (atrN * sqrtN);
    }

    @Override
    public double getValue() {
        return value;
    }

}
