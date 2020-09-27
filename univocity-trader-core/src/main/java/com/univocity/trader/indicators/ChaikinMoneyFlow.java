package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

public class ChaikinMoneyFlow extends SingleValueIndicator {

    private double value;
    private final CloseLocationValue clv;
    private final Volume volume;
    private CircularList mfl;

    public ChaikinMoneyFlow(TimeInterval interval) {
        this(20, interval);
    }

    public ChaikinMoneyFlow(int length, TimeInterval interval) {
        super(interval, null);
        this.clv = new CloseLocationValue(interval);
        this.volume = new Volume(length, interval);
        this.mfl = new CircularList(length);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        if(clv.accumulate(candle)){
            volume.accumulate(candle);
            mfl.accumulate(clv.getValue() * candle.volume, updating);
            this.value = mfl.sum() / volume.getValue();
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
        return new Indicator[]{clv, volume};
    }
}
