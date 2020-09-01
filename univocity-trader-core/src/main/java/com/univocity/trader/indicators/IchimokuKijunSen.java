package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.TimeInterval;

public class IchimokuKijunSen extends AbstractIchimokuLineIndicator {

    public IchimokuKijunSen(TimeInterval interval) {
        this(26, interval);
    }

    public IchimokuKijunSen(int length, TimeInterval interval) {
        super(length, interval);
    }

}
