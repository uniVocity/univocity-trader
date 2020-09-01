package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.TimeInterval;

public class IchimokuSenkouSpanB extends AbstractIchimokuLineIndicator {

    public IchimokuSenkouSpanB(TimeInterval interval) {
        this(52, interval);
    }

    public IchimokuSenkouSpanB(int length, TimeInterval interval) {
        super(length, interval);
    }

}
