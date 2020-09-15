package com.univocity.trader.exchange.binance.futures.model.enums;


public enum MarginTransferType {
    IN("1"),
    OUT("2");

    private final String code;

    MarginTransferType(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
