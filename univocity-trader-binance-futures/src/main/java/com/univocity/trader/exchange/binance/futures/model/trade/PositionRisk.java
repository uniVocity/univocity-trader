package com.univocity.trader.exchange.binance.futures.model.trade;

import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import com.univocity.trader.exchange.binance.futures.model.enums.PositionSide;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class PositionRisk {

    private BigDecimal entryPrice;      //": "0.00000", // 开仓均价
    private String marginType;      //": "isolated", // 逐仓模式或全仓模式
    private Boolean isAutoAddMargin;      //": "false",
    private BigDecimal isolatedMargin;      //": "0.00000000", // 逐仓保证金
    private Integer leverage;      //": "10", // 当前杠杆倍数
    private BigDecimal liquidationPrice;      //": "0", // 参考强平价格
    private BigDecimal markPrice;      //": "6679.50671178",   // 当前标记价格
    private Long maxNotionalValue;      //": "20000000", // 当前杠杆倍数允许的名义价值上限
    private BigDecimal positionAmt;      //": "1.000", // 头寸数量，符号代表多空方向, 正数为多，负数为空
    private String symbol;      //": "BTCUSDT", // 交易对
    private BigDecimal unRealizedProfit;      //": "0.00000000", // 持仓未实现盈亏
    private PositionSide positionSide;      //": "BOTH", // 持仓方向

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("entryPrice", entryPrice)
                .append("marginType", marginType).append("isAutoAddMargin", isAutoAddMargin)
                .append("isolatedMargin", isolatedMargin).append("leverage", leverage)
                .append("liquidationPrice", liquidationPrice).append("markPrice", markPrice)
                .append("maxNotionalValue", maxNotionalValue).append("positionAmt", positionAmt)
                .append("symbol", symbol).append("unRealizedProfit", unRealizedProfit)
                .append("positionSide", positionSide).toString();
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public String getMarginType() {
        return marginType;
    }

    public void setMarginType(String marginType) {
        this.marginType = marginType;
    }

    public Boolean getAutoAddMargin() {
        return isAutoAddMargin;
    }

    public void setAutoAddMargin(Boolean autoAddMargin) {
        isAutoAddMargin = autoAddMargin;
    }

    public BigDecimal getIsolatedMargin() {
        return isolatedMargin;
    }

    public void setIsolatedMargin(BigDecimal isolatedMargin) {
        this.isolatedMargin = isolatedMargin;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
    }

    public BigDecimal getLiquidationPrice() {
        return liquidationPrice;
    }

    public void setLiquidationPrice(BigDecimal liquidationPrice) {
        this.liquidationPrice = liquidationPrice;
    }

    public BigDecimal getMarkPrice() {
        return markPrice;
    }

    public void setMarkPrice(BigDecimal markPrice) {
        this.markPrice = markPrice;
    }

    public Long getMaxNotionalValue() {
        return maxNotionalValue;
    }

    public void setMaxNotionalValue(Long maxNotionalValue) {
        this.maxNotionalValue = maxNotionalValue;
    }

    public BigDecimal getPositionAmt() {
        return positionAmt;
    }

    public void setPositionAmt(BigDecimal positionAmt) {
        this.positionAmt = positionAmt;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getUnRealizedProfit() {
        return unRealizedProfit;
    }

    public void setUnRealizedProfit(BigDecimal unRealizedProfit) {
        this.unRealizedProfit = unRealizedProfit;
    }

    public PositionSide getPositionSide() {
        return positionSide;
    }

    public void setPositionSide(PositionSide positionSide) {
        this.positionSide = positionSide;
    }
}
