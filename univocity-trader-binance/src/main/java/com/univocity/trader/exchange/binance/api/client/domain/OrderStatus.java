package com.univocity.trader.exchange.binance.api.client.domain;

import com.fasterxml.jackson.annotation.*;

/**
 * Status of a submitted order.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum OrderStatus {
	NEW,
	PARTIALLY_FILLED,
	FILLED,
	CANCELED,
	PENDING_CANCEL,
	REJECTED,
	EXPIRED
}
