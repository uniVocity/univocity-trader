package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class ChaikinMoneyFlowIndicator extends MultiValueIndicator {

    private double value;

    private final CloseLocationValue clv;
    private final Volume volume;

    private CircularList mfl;

    public ChaikinMoneyFlowIndicator(TimeInterval interval) {
        this(20, interval);
    }

    public ChaikinMoneyFlowIndicator(int length, TimeInterval interval) {
        this(length, interval, null);
    }

    public ChaikinMoneyFlowIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        this.clv = new CloseLocationValue(interval);
        this.volume = new Volume(length, interval);
        this.mfl = new CircularList(length);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
        mfl.accumulate(getMFL(candle), updating);

        int startIndex = values.getStartingIndex();
        int remaining = mfl.size();
        double sumOfMoneyFlowVolume = 0;

        while (remaining > 0) {
            sumOfMoneyFlowVolume = sumOfMoneyFlowVolume + mfl.get(startIndex++);
            remaining--;
        }

        this.value = sumOfMoneyFlowVolume / volume.getValue();

        return true;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{clv, volume};
    }

    private double getMFL(Candle candle) {
        if(clv.accumulate(candle)) {
            volume.accumulate(candle);
        }
        return clv.getValue() * candle.volume;
    }

}
