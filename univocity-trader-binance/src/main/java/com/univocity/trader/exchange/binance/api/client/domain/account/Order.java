package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.domain.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * Trade order information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements OrderDetails {

	/**
	 * Symbol that the order was put on.
	 */
	private String symbol;

	/**
	 * Order id.
	 */
	private Long orderId;

	/**
	 * Client order id.
	 */
	private String clientOrderId;

	/**
	 * Price.
	 */
	private String price;

	/**
	 * Original quantity.
	 */
	private String origQty;

	/**
	 * Original quantity.
	 */
	private String executedQty;

	/**
	 * Order status.
	 */
	private OrderStatus status;

	/**
	 * Time in force to indicate how long will the order remain active.
	 */
	private TimeInForce timeInForce;

	/**
	 * Type of order.
	 */
	private OrderType type;

	/**
	 * Buy/Sell order side.
	 */
	private OrderSide side;

	/**
	 * Used with stop orders.
	 */
	private String stopPrice;

	/**
	 * Used with iceberg orders.
	 */
	private String icebergQty;

	/**
	 * Order timestamp.
	 */
	private long time;

	/**
	 * Used to calculate the average price
	 */
	private String cummulativeQuoteQty;

	@Override
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	@Override
	public String getClientOrderId() {
		return clientOrderId;
	}

	public void setClientOrderId(String clientOrderId) {
		this.clientOrderId = clientOrderId;
	}

	@Override
	public String getPrice() {
		return price;
	}

	public double getPriceAmount() {
		return Double.parseDouble(price);
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Override
	public String getOrigQty() {
		return origQty;
	}

	public void setOrigQty(String origQty) {
		this.origQty = origQty;
	}

	public double getExecutedQtyAmount() {
		return Double.parseDouble(executedQty);
	}

	@Override
	public String getExecutedQty() {
		return executedQty;
	}

	public void setExecutedQty(String executedQty) {
		this.executedQty = executedQty;
	}

	@Override
	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public TimeInForce getTimeInForce() {
		return timeInForce;
	}

	public void setTimeInForce(TimeInForce timeInForce) {
		this.timeInForce = timeInForce;
	}

	@Override
	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	@Override
	public OrderSide getSide() {
		return side;
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}

	public String getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(String stopPrice) {
		this.stopPrice = stopPrice;
	}

	public String getIcebergQty() {
		return icebergQty;
	}

	public void setIcebergQty(String icebergQty) {
		this.icebergQty = icebergQty;
	}

	@Override
	public Long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public String getCummulativeQuoteQty() {
		return cummulativeQuoteQty;
	}

	public double getCummulativeQuoteQtyAmount() {
		return Double.parseDouble(cummulativeQuoteQty);
	}

	public void setCummulativeQuoteQty(String cummulativeQuoteQty) {
		this.cummulativeQuoteQty = cummulativeQuoteQty;
	}



	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("symbol", symbol)
				.append("orderId", orderId)
				.append("clientOrderId", clientOrderId)
				.append("price", price)
				.append("origQty", origQty)
				.append("executedQty", executedQty)
				.append("status", status)
				.append("timeInForce", timeInForce)
				.append("type", type)
				.append("side", side)
				.append("stopPrice", stopPrice)
				.append("icebergQty", icebergQty)
				.append("time", time)
				.append("cummulativeQuoteQty", cummulativeQuoteQty)
				.toString();
	}
}
