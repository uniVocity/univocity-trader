package com.univocity.trader.exchange.binance.futures.model.trade;

import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class Leverage {

    private BigDecimal leverage;

    private Double maxNotionalValue;

    private String symbol;

    public BigDecimal getLeverage() {
        return leverage;
    }

    public void setLeverage(BigDecimal leverage) {
        this.leverage = leverage;
    }

    public Double getMaxNotionalValue() {
        return maxNotionalValue;
    }

    public void setMaxNotionalValue(Double maxNotionalValue) {
        this.maxNotionalValue = maxNotionalValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("leverage", leverage)
                .append("maxNotionalValue", maxNotionalValue).append("symbol", symbol).toString();
    }
}
