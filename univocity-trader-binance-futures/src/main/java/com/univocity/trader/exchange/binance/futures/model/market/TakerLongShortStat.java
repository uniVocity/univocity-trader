package com.univocity.trader.exchange.binance.futures.model.market;

import java.math.BigDecimal;

public class TakerLongShortStat {
    
    private BigDecimal buySellRatio;
    private BigDecimal sellVol;
    private BigDecimal buyVol;
    private Long timestamp;

    public BigDecimal getBuySellRatio() {
        return buySellRatio;
    }

    public void setBuySellRatio(BigDecimal buySellRatio) {
        this.buySellRatio = buySellRatio;
    }

    public BigDecimal getSellVol() {
        return sellVol;
    }

    public void setSellVol(BigDecimal sellVol) {
        this.sellVol = sellVol;
    }

    public BigDecimal getBuyVol() {
        return buyVol;
    }

    public void setBuyVol(BigDecimal buyVol) {
        this.buyVol = buyVol;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TakerLongShortStat{" +
                "buySellRatio=" + buySellRatio +
                ", sellVol=" + sellVol +
                ", buyVol=" + buyVol +
                ", timestamp=" + timestamp +
                '}';
    }
}
