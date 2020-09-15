package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

public enum PeriodType {
    _5m("5m"),
    _15m("15m"),
    _30m("30m"),
    _1h("1h"),
    _2h("2h"),
    _4h("4h"),
    _6h("6h"),
    _12h("12h"),
    _1d("1d");

    private final String code;

    PeriodType(String code) {
        this.code = code;
    }


    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    private static final EnumLookup<PeriodType> lookup = new EnumLookup<>(PeriodType.class);

    public static PeriodType lookup(String name) {
        return lookup.lookup(name);
    }
}
