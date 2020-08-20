package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

public class CovarianceIndicator extends MultiValueIndicator {

    private double value;

    private final CircularList closeList;
    private final CircularList volumeList;

    private final Volume volume;
    private final MovingAverage sma1;
    private final MovingAverage sma2;

    public CovarianceIndicator(TimeInterval interval) {
        this(20, new Volume(2, interval), interval);
    }

    public CovarianceIndicator(int length, Volume volume, TimeInterval interval) {
        super(length, interval, null);
        this.closeList = new CircularList(length);
        this.volumeList = new CircularList(length);
        this.volume = volume;
        this.sma1 = new MovingAverage(length, interval, c -> c.close);
        this.sma2 = new MovingAverage(length, interval, c -> c.volume);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
        if (volume.accumulate(candle)) {

            closeList.accumulate(candle.close, updating);
            volumeList.accumulate(volume.getValue(), updating);

            sma1.accumulate(candle);
            sma2.accumulate(candle);

            double average1 = sma1.getValue();
            double average2 = sma2.getValue();

            final int count = this.values.size();
            int from = values.getStartingIndex();
            int c = count;

            double covariance = 0;
            while (c-- > 0) {
                covariance += (closeList.get(from) - average1) * (volumeList.get(from) - average2);
                from = (from + 1) % count;
            }

            this.value = covariance / count;

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
        return new Indicator[]{volume, sma1, sma2};
    }
}
