package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.TimeInterval;

public class IchimokuTenkanSen extends AbstractIchimokuLineIndicator {

    public IchimokuTenkanSen(TimeInterval interval) {
        this(9, interval);
    }

    public IchimokuTenkanSen(int length, TimeInterval interval) {
        super(length, interval);
    }

}
