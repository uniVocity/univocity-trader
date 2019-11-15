package com.univocity.trader.exchange.binance.api.client.domain.event;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.domain.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * Order or trade report update event.
 *
 * This event is embedded as part of a user data update event.
 *
 * @see UserDataUpdateEvent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderTradeUpdateEvent {

	@JsonProperty("e")
	private String eventType;

	@JsonProperty("E")
	private Long eventTime;

	@JsonProperty("s")
	private String symbol;

	@JsonProperty("c")
	private String newClientOrderId;

	/**
	 * Buy/Sell order side.
	 */
	@JsonProperty("S")
	private OrderSide side;

	/**
	 * Type of order.
	 */
	@JsonProperty("o")
	private OrderType type;

	/**
	 * Time in force to indicate how long will the order remain active.
	 */
	@JsonProperty("f")
	private TimeInForce timeInForce;

	/**
	 * Original quantity in the order.
	 */
	@JsonProperty("q")
	private String originalQuantity;

	/**
	 * Price.
	 */
	@JsonProperty("p")
	private String price;

	/**
	 * Type of execution.
	 */
	@JsonProperty("x")
	private ExecutionType executionType;

	/**
	 * Status of the order.
	 */
	@JsonProperty("X")
	private OrderStatus orderStatus;

	/**
	 * Reason why the order was rejected.
	 */
	@JsonProperty("r")
	private OrderRejectReason orderRejectReason;

	/**
	 * Order id.
	 */
	@JsonProperty("i")
	private Long orderId;

	/**
	 * Quantity of the last filled trade.
	 */
	@JsonProperty("l")
	private String quantityLastFilledTrade;

	/**
	 * Accumulated quantity of filled trades on this order.
	 */
	@JsonProperty("z")
	private String accumulatedQuantity;

	/**
	 * Price of last filled trade.
	 */
	@JsonProperty("L")
	private String priceOfLastFilledTrade;

	/**
	 * Commission.
	 */
	@JsonProperty("n")
	private String commission;

	/**
	 * Asset on which commission is taken
	 */
	@JsonProperty("N")
	private String commissionAsset;

	/**
	 * Order/trade time.
	 */
	@JsonProperty("T")
	private Long orderTradeTime;

	/**
	 * Trade id.
	 */
	@JsonProperty("t")
	private Long tradeId;

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Long getEventTime() {
		return eventTime;
	}

	public void setEventTime(Long eventTime) {
		this.eventTime = eventTime;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getNewClientOrderId() {
		return newClientOrderId;
	}

	public void setNewClientOrderId(String newClientOrderId) {
		this.newClientOrderId = newClientOrderId;
	}

	public OrderSide getSide() {
		return side;
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public TimeInForce getTimeInForce() {
		return timeInForce;
	}

	public void setTimeInForce(TimeInForce timeInForce) {
		this.timeInForce = timeInForce;
	}

	public String getOriginalQuantity() {
		return originalQuantity;
	}

	public void setOriginalQuantity(String originalQuantity) {
		this.originalQuantity = originalQuantity;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public ExecutionType getExecutionType() {
		return executionType;
	}

	public void setExecutionType(ExecutionType executionType) {
		this.executionType = executionType;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public OrderRejectReason getOrderRejectReason() {
		return orderRejectReason;
	}

	public void setOrderRejectReason(OrderRejectReason orderRejectReason) {
		this.orderRejectReason = orderRejectReason;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getQuantityLastFilledTrade() {
		return quantityLastFilledTrade;
	}

	public void setQuantityLastFilledTrade(String quantityLastFilledTrade) {
		this.quantityLastFilledTrade = quantityLastFilledTrade;
	}

	public String getAccumulatedQuantity() {
		return accumulatedQuantity;
	}

	public void setAccumulatedQuantity(String accumulatedQuantity) {
		this.accumulatedQuantity = accumulatedQuantity;
	}

	public String getPriceOfLastFilledTrade() {
		return priceOfLastFilledTrade;
	}

	public void setPriceOfLastFilledTrade(String priceOfLastFilledTrade) {
		this.priceOfLastFilledTrade = priceOfLastFilledTrade;
	}

	public String getCommission() {
		return commission;
	}

	public void setCommission(String commission) {
		this.commission = commission;
	}

	public String getCommissionAsset() {
		return commissionAsset;
	}

	public void setCommissionAsset(String commissionAsset) {
		this.commissionAsset = commissionAsset;
	}

	public Long getOrderTradeTime() {
		return orderTradeTime;
	}

	public void setOrderTradeTime(Long orderTradeTime) {
		this.orderTradeTime = orderTradeTime;
	}

	public Long getTradeId() {
		return tradeId;
	}

	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("eventType", eventType)
				.append("eventTime", eventTime)
				.append("symbol", symbol)
				.append("newClientOrderId", newClientOrderId)
				.append("side", side)
				.append("type", type)
				.append("timeInForce", timeInForce)
				.append("originalQuantity", originalQuantity)
				.append("price", price)
				.append("executionType", executionType)
				.append("orderStatus", orderStatus)
				.append("orderRejectReason", orderRejectReason)
				.append("orderId", orderId)
				.append("quantityLastFilledTrade", quantityLastFilledTrade)
				.append("accumulatedQuantity", accumulatedQuantity)
				.append("priceOfLastFilledTrade", priceOfLastFilledTrade)
				.append("commission", commission)
				.append("commissionAsset", commissionAsset)
				.append("orderTradeTime", orderTradeTime)
				.append("tradeId", tradeId)
				.toString();
	}
}
