package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class AccelerationDecelerationIndicator extends SingleValueIndicator {

    private double value;

    private final AwesomeOscillator awesome;
    private final MovingAverage ma;

    public AccelerationDecelerationIndicator(TimeInterval interval) {
        this(5, 34, interval);
    }

    public AccelerationDecelerationIndicator(int lengthShort, int lengthLong, TimeInterval interval) {
        super(interval, null);
        awesome = new AwesomeOscillator(lengthShort, lengthLong, interval);
        ma = new MovingAverage(lengthShort, interval, c -> awesome.getValue());
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        if(awesome.accumulate(candle)) {
            ma.accumulate(candle);

            this.value = awesome.getValue() - ma.getValue();

            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{awesome, ma};
    }
}
