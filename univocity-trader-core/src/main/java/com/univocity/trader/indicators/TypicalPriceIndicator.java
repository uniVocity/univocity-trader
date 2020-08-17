package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

import java.util.function.ToDoubleFunction;

public class TypicalPriceIndicator extends SingleValueIndicator {

    private double value;

    public TypicalPriceIndicator(TimeInterval interval) {
        this(interval, null);
    }

    public TypicalPriceIndicator(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(interval, valueGetter == null ? c -> c.close : valueGetter);
    }

    @Override
    protected boolean process(Candle candle, double value, boolean updating) {
        double maxPrice = candle.high;
        double minPrice = candle.low;
        double closePrice = candle.close;
        this.value = (maxPrice + minPrice + closePrice) / 3;
        return true;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{};
    }

}
