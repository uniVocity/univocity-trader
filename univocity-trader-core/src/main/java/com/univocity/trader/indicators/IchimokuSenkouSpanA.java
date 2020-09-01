package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.SingleValueCalculationIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;

public class IchimokuSenkouSpanA extends SingleValueCalculationIndicator {

    private final IchimokuTenkanSen conversionLine;
    private final IchimokuKijunSen baseLine;

    public IchimokuSenkouSpanA(TimeInterval interval) {
        this(interval, new IchimokuTenkanSen(interval), new IchimokuKijunSen(interval));
    }

    public IchimokuSenkouSpanA(TimeInterval interval, int barCountConversionLine, int barCountBaseLine) {
        this(interval, new IchimokuTenkanSen(barCountConversionLine, interval),
                new IchimokuKijunSen(barCountBaseLine, interval));
    }

    public IchimokuSenkouSpanA(TimeInterval interval, IchimokuTenkanSen conversionLine,
                               IchimokuKijunSen baseLine) {
        super(interval);
        this.conversionLine = conversionLine;
        this.baseLine = baseLine;
    }

    @Override
    protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
        if (conversionLine.accumulate(candle)) {
            baseLine.accumulate(candle);
            return (conversionLine.getValue() + baseLine.getValue()) / 2;
        }
        return Double.NaN;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{conversionLine, baseLine};
    }

}
