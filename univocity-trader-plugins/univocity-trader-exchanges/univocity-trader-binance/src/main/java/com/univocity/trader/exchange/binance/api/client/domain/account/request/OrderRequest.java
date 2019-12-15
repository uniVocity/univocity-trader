package com.univocity.trader.exchange.binance.api.client.domain.account.request;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * Base request parameters for order-related methods.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

	private final String symbol;

	private Long recvWindow;

	private Long timestamp;

	public OrderRequest(String symbol) {
		this.symbol = symbol;
		this.timestamp = System.currentTimeMillis();
		this.recvWindow = BinanceApiConstants.DEFAULT_RECEIVING_WINDOW;
	}

	public String getSymbol() {
		return symbol;
	}

	public Long getRecvWindow() {
		return recvWindow;
	}

	public OrderRequest recvWindow(Long recvWindow) {
		this.recvWindow = recvWindow;
		return this;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public OrderRequest timestamp(Long timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("symbol", symbol)
				.append("recvWindow", recvWindow)
				.append("timestamp", timestamp)
				.toString();
	}
}
