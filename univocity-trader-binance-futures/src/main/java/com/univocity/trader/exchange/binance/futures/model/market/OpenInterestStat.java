package com.univocity.trader.exchange.binance.futures.model.market;

import java.math.BigDecimal;

public class OpenInterestStat {
    
    private String symbol;
    private BigDecimal sumOpenInterest;
    private BigDecimal sumOpenInterestValue;
    private Long timestamp;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getSumOpenInterest() {
        return sumOpenInterest;
    }

    public void setSumOpenInterest(BigDecimal sumOpenInterest) {
        this.sumOpenInterest = sumOpenInterest;
    }

    public BigDecimal getSumOpenInterestValue() {
        return sumOpenInterestValue;
    }

    public void setSumOpenInterestValue(BigDecimal sumOpenInterestValue) {
        this.sumOpenInterestValue = sumOpenInterestValue;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OpenInterestStat{" +
                "symbol='" + symbol + '\'' +
                ", sumOpenInterest=" + sumOpenInterest +
                ", sumOpenInterestValue=" + sumOpenInterestValue +
                ", timestamp=" + timestamp +
                '}';
    }
}
