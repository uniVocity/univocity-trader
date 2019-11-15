package com.univocity.trader.account;

import java.math.*;

public class DefaultOrder extends OrderRequest implements Order {

	private String orderId;
	private BigDecimal executedQuantity;
	private Long time;
	private Order.Status status;

	public DefaultOrder(String assettSymbol, String fundSymbol, Side side) {
		super(assettSymbol, fundSymbol, side);
	}

	public DefaultOrder(Order order) {
		super(order.getAssetsSymbol(), order.getFundsSymbol(), order.getSide());
		this.setOrderId(order.getOrderId());
		this.setType(order.getType());
		this.setTime(order.getTime());
		this.setQuantity(order.getQuantity());
		this.setPrice(order.getPrice());
	}

	@Override
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public BigDecimal getExecutedQuantity() {
		return executedQuantity;
	}

	public void setExecutedQuantity(BigDecimal executedQuantity) {
		this.executedQuantity = executedQuantity;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	@Override
	public void cancel() {
		this.status = Status.CANCELLED;
	}

	@Override
	public String toString() {
		return "DefaultOrder{" +
				", orderId='" + orderId + '\'' +
				", executedQuantity=" + executedQuantity +
				", time=" + time +
				", status=" + status +
				'}';
	}
}
