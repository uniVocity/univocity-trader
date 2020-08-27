package com.univocity.trader.indicators;

import com.univocity.trader.candles.Aggregator;
import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.Statistic;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

import static com.univocity.trader.indicators.base.AggregatedTicksIndicator.getAggregator;

public class PearsonCorrelation extends Statistic {

    private CircularList l1;
    private CircularList l2;

    private Indicator indicator1;
    private Indicator indicator2;

    private Aggregator aggregator1;
    private Aggregator aggregator2;

    public PearsonCorrelation(int length, TimeInterval interval, ToDoubleFunction<Candle> indicator1, ToDoubleFunction<Candle> indicator2) {
        super(length, interval, indicator1, indicator2);
    }

    public PearsonCorrelation(int length, Indicator indicator1, ToDoubleFunction<Candle> indicator2) {
        super(length, indicator1, indicator2);
    }

    public PearsonCorrelation(int length, ToDoubleFunction<Candle> indicator1, Indicator indicator2) {
        super(length, indicator1, indicator2);
    }

    public PearsonCorrelation(int length, Indicator indicator1, Indicator indicator2) {
        super(length, indicator1, indicator2);
    }

    @Override
    protected void initialize(Indicator indicator1, Indicator indicator2) {
        this.l1 = new CircularList(length);
        this.l2 = new CircularList(length);
        this.indicator1 = indicator1;
        this.indicator2 = indicator2;
        aggregator1 = getAggregator(indicator1);
        aggregator2 = getAggregator(indicator2);
    }

    @Override
    protected boolean indicatorsAccumulated(Candle candle) {
        return indicator1.accumulate(candle) | indicator2.accumulate(candle);
    }

    @Override
    protected double calculate() {
        l1.accumulate(indicator1.getValue(), aggregator1 != null && aggregator1.getPartial() != null);
        l2.accumulate(indicator2.getValue(), aggregator2 != null && aggregator2.getPartial() != null);

        int from1 = l1.getStartingIndex();
        int from2 = l2.getStartingIndex();
        int c = Math.min(l1.size(), l2.size());

        double Sx = 0;
        double Sy = 0;
        double Sxx = 0;
        double Syy = 0;
        double Sxy = 0;

        while (c-- > 0) {

            double x = l1.get(from1);
            double y = l2.get(from2);

            Sx += x;
            Sy += y;
            Sxy += x * y;
            Sxx += x * x;
            Syy += y * y;

            from1 = (from1 + 1) % length;
            from2 = (from2 + 1) % length;
        }

        double n = l1.capacity();
        double toSqrt = ((n * Sxx) - (Sx * Sx)) * ((n * Syy) - (Sy * Sy));

        if(toSqrt > 0) {
            return ((n * Sxy) - (Sx * Sy)) / (Math.sqrt(toSqrt));
        }

        return Double.NaN;

    }

}
