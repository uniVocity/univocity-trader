package com.univocity.trader.account;

import java.math.*;
import java.util.*;

import static com.univocity.trader.account.Balance.*;

public class DefaultOrder extends OrderRequest implements Order {

	private String orderId;
	private BigDecimal executedQuantity;
	private Order.Status status;
	private BigDecimal feesPaid = BigDecimal.ZERO;
	private List<Order> attachments;

	public DefaultOrder(String assetSymbol, String fundSymbol, Order.Side side, Trade.Side tradeSide, long time) {
		this(assetSymbol, fundSymbol, side, tradeSide, time, null);
	}

	public DefaultOrder(String assetSymbol, String fundSymbol, Order.Side side, Trade.Side tradeSide, long time,
			List<Order> attachments) {
		super(assetSymbol, fundSymbol, side, tradeSide, time, null);
		this.attachments = attachments;
	}

	public DefaultOrder(Order order) {
		super(order.getAssetsSymbol(), order.getFundsSymbol(), order.getSide(), order.getTradeSide(), order.getTime(),
				null);
		this.setOrderId(order.getOrderId());
		this.setType(order.getType());
		this.setQuantity(order.getQuantity());
		this.setPrice(order.getPrice());
		this.attachments = order.getAttachments();
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
	public BigDecimal getFeesPaid() {
		return feesPaid;
	}

	public void setFeesPaid(BigDecimal feesPaid) {
		this.feesPaid = round(feesPaid);
	}

	@Override
	public String toString() {
		return print(0);
	}

	public List<Order> getAttachments() {
		return attachments == null ? null : Collections.unmodifiableList(attachments);
	}
}
