package com.univocity.trader.exchange.binance.futures.model.trade;

import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import com.univocity.trader.exchange.binance.futures.model.enums.OrderSide;
import com.univocity.trader.exchange.binance.futures.model.enums.OrderStatus;
import com.univocity.trader.exchange.binance.futures.model.enums.OrderType;
import com.univocity.trader.exchange.binance.futures.model.enums.PositionSide;
import com.univocity.trader.exchange.binance.futures.model.enums.TimeInForce;
import com.univocity.trader.exchange.binance.futures.model.enums.WorkingType;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class FuturesOrder {

    private String symbol; //交易对

    private OrderSide side; //买卖方向 SELL, BUY

    private PositionSide positionSide; //持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT

    private OrderType type; //订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET

    private Boolean reduceOnly; //true, false; 非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。

    private BigDecimal origQty;

    private BigDecimal price;

    private String clientOrderId;

    private BigDecimal stopPrice;

    private BigDecimal activationPrice;

    private Boolean closePosition;

    private BigDecimal cumQuote;

    private BigDecimal executedQty;

    private Long orderId;

    private OrderStatus status;

    private TimeInForce timeInForce;

    private Long updateTime;

    private WorkingType workingType;

    public FuturesOrder(){}

    /**
     * Creates a new order with all required parameters.
     */
    public FuturesOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType type, TimeInForce timeInForce, BigDecimal quantity) {
        this.symbol = symbol;
        this.side = side;
        this.positionSide = positionSide;
        this.type = type;
        this.timeInForce = timeInForce;
        this.origQty = quantity;
        this.reduceOnly = true;
        this.workingType = WorkingType.CONTRACT_PRICE;
        this.updateTime = System.currentTimeMillis();
    }

    /**
     * Creates a new order with all required parameters plus price, which is optional for MARKET orders.
     */
    public FuturesOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType type, TimeInForce timeInForce, BigDecimal quantity, BigDecimal price) {
        this(symbol, side, positionSide, type, timeInForce, quantity);
        this.price = price;
    }


    public FuturesOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType type, TimeInForce timeInForce, BigDecimal quantity, BigDecimal price, BigDecimal stopPrice) {
        this(symbol, side, positionSide, type, timeInForce, quantity, price);
        this.stopPrice = stopPrice;
    }

    public FuturesOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType type, TimeInForce timeInForce, BigDecimal stopPrice, BigDecimal activationPrice, Boolean closePosition) {
        this(symbol, side, positionSide, type, timeInForce, null, null);
        this.stopPrice = stopPrice;
        this.closePosition = closePosition;
        this.activationPrice = stopPrice;
    }

    public String getClientOrderId() {
        return clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public BigDecimal getCumQuote() {
        return cumQuote;
    }

    public void setCumQuote(BigDecimal cumQuote) {
        this.cumQuote = cumQuote;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public void setExecutedQty(BigDecimal executedQty) {
        this.executedQty = executedQty;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrigQty() {
        return origQty;
    }

    public void setOrigQty(BigDecimal origQty) {
        this.origQty = origQty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getReduceOnly() {
        return reduceOnly;
    }

    public void setReduceOnly(Boolean reduceOnly) {
        this.reduceOnly = reduceOnly;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public PositionSide getPositionSide() {
        return positionSide;
    }

    public void setPositionSide(PositionSide positionSide) {
        this.positionSide = positionSide;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(BigDecimal stopPrice) {
        this.stopPrice = stopPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(TimeInForce timeInForce) {
        this.timeInForce = timeInForce;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public WorkingType getWorkingType() {
        return workingType;
    }

    public void setWorkingType(WorkingType workingType) {
        this.workingType = workingType;
    }

    public Boolean getClosePosition() {
        return closePosition;
    }

    public void setClosePosition(Boolean closePosition) {
        this.closePosition = closePosition;
    }

    public BigDecimal getActivationPrice() {
        return activationPrice;
    }

    public void setActivationPrice(BigDecimal activationPrice) {
        this.activationPrice = activationPrice;
    }

    public static FuturesOrder marketBuy(String symbol, BigDecimal quantity) {
        return new FuturesOrder(symbol, OrderSide.BUY, PositionSide.LONG, OrderType.MARKET, null, quantity);
    }

    public static FuturesOrder marketSell(String symbol, BigDecimal quantity) {
        return new FuturesOrder(symbol, OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null, quantity);
    }

    public static FuturesOrder limitBuy(String symbol, TimeInForce timeInForce, BigDecimal quantity, BigDecimal price) {
        return new FuturesOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, timeInForce, quantity, price);
    }

    public static FuturesOrder limitOCOBuy(String symbol, TimeInForce timeInForce, BigDecimal quantity, BigDecimal price, BigDecimal stopPrice) {
        return new FuturesOrder(symbol, OrderSide.BUY, PositionSide.LONG, OrderType.TAKE_PROFIT, timeInForce, quantity, price, stopPrice);
    }

    public static FuturesOrder limitSell(String symbol, TimeInForce timeInForce, BigDecimal quantity, BigDecimal price) {
        return new FuturesOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, timeInForce, quantity, price);
    }

    public static FuturesOrder limitOCOSell(String symbol, TimeInForce timeInForce, BigDecimal quantity, BigDecimal price, BigDecimal stopPrice) {
        return new FuturesOrder(symbol, OrderSide.SELL, PositionSide.SHORT, OrderType.LIMIT, timeInForce, quantity, price, stopPrice);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("clientOrderId", clientOrderId).append("cumQuote", cumQuote).append("executedQty", executedQty)
                .append("orderId", orderId).append("origQty", origQty).append("price", price).append("reduceOnly", reduceOnly)
                .append("side", side).append("positionSide", positionSide).append("status", status).append("stopPrice", stopPrice)
                .append("symbol", symbol).append("timeInForce", timeInForce).append("closePosition", closePosition)
                .append("type", type).append("updateTime", updateTime).append("workingType", workingType).toString();
    }
}
