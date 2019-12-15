package com.univocity.trader.exchange.binance.api.client.domain.account.request;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import org.apache.commons.lang3.builder.*;

/**
 * A specialized order request with additional filters.
 */
public class OrderStatusRequest extends OrderRequest {

	private Long orderId;

	private String origClientOrderId;

	public OrderStatusRequest(String symbol, Long orderId) {
		super(symbol);
		this.orderId = orderId;
	}

	public OrderStatusRequest(String symbol, String origClientOrderId) {
		super(symbol);
		this.origClientOrderId = origClientOrderId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public OrderStatusRequest orderId(Long orderId) {
		this.orderId = orderId;
		return this;
	}

	public String getOrigClientOrderId() {
		return origClientOrderId;
	}

	public OrderStatusRequest origClientOrderId(String origClientOrderId) {
		this.origClientOrderId = origClientOrderId;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("orderId", orderId)
				.append("origClientOrderId", origClientOrderId)
				.toString();
	}
}
