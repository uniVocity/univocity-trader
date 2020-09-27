package com.univocity.trader.exchange.binance.futures.model.market;

import java.math.BigDecimal;

public class CommonLongShortRatio {
    
    private String symbol;
    private BigDecimal longAccount;
    private BigDecimal longShortRatio;
    private BigDecimal shortAccount;
    private Long timestamp;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getLongAccount() {
        return longAccount;
    }

    public void setLongAccount(BigDecimal longAccount) {
        this.longAccount = longAccount;
    }

    public BigDecimal getLongShortRatio() {
        return longShortRatio;
    }

    public void setLongShortRatio(BigDecimal longShortRatio) {
        this.longShortRatio = longShortRatio;
    }

    public BigDecimal getShortAccount() {
        return shortAccount;
    }

    public void setShortAccount(BigDecimal shortAccount) {
        this.shortAccount = shortAccount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CommonLongShortRatio{" +
                "symbol='" + symbol + '\'' +
                ", longAccount=" + longAccount +
                ", longShortRatio=" + longShortRatio +
                ", shortAccount=" + shortAccount +
                ", timestamp=" + timestamp +
                '}';
    }
}
