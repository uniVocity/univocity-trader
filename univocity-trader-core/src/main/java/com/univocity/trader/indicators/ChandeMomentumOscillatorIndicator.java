package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class ChandeMomentumOscillatorIndicator extends MultiValueIndicator {

    private double value;

    private int count;
    private double sumOfGains;
    private double sumOfLosses;

    private final GainIndicator gainIndicator;
    private final LossIndicator lossIndicator;

    public ChandeMomentumOscillatorIndicator(TimeInterval interval) {
        this(9, interval);
    }

    public ChandeMomentumOscillatorIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public ChandeMomentumOscillatorIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        gainIndicator = new GainIndicator(length, interval, c -> c.close);
        lossIndicator = new LossIndicator(length, interval, c -> c.close);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {

        if (gainIndicator.accumulate(candle)) {

            lossIndicator.accumulate(candle);

            sumOfGains = sumOfGains + gainIndicator.getValue();
            sumOfLosses = sumOfLosses + lossIndicator.getValue();

            this.value = ((sumOfGains - sumOfLosses) / (sumOfGains + sumOfLosses)) * 100;

            count++;

            if(count == values.capacity()) {
                // remover o candle mais antigo?
            }

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
        return new Indicator[]{gainIndicator, lossIndicator};
    }

}
