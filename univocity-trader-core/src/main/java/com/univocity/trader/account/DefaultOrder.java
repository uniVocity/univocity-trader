package com.univocity.trader.account;

import java.math.*;

import static com.univocity.trader.account.Balance.*;

public class DefaultOrder extends OrderRequest implements Order {

	private String orderId;
	private BigDecimal executedQuantity;
	private Order.Status status;

	public DefaultOrder(String assetSymbol, String fundSymbol, Side side, long time) {
		super(assetSymbol, fundSymbol, side, time);
	}

	public DefaultOrder(Order order) {
		super(order.getAssetsSymbol(), order.getFundsSymbol(), order.getSide(), order.getTime());
		this.setOrderId(order.getOrderId());
		this.setType(order.getType());
		this.setQuantity(round(order.getQuantity()));
		this.setPrice(round(order.getPrice()));
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
		this.executedQuantity = round(executedQuantity);
	}

	@Override
	public void setPrice(BigDecimal price) {
		super.setPrice(round(price));
	}

	@Override
	public void setQuantity(BigDecimal quantity) {
		super.setQuantity(round(quantity));
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public void cancel() {
		this.status = Status.CANCELLED;
	}

	public boolean isCancelled() {
		return this.status == Status.CANCELLED;
	}

	@Override
	public String toString() {
		return print(0);
	}
}
