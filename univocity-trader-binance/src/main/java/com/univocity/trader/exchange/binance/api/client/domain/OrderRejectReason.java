package com.univocity.trader.exchange.binance.api.client.domain;

import com.fasterxml.jackson.annotation.*;

/**
 * Order reject reason values.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum OrderRejectReason {
	NONE,
	UNKNOWN_INSTRUMENT,
	MARKET_CLOSED,
	PRICE_QTY_EXCEED_HARD_LIMITS,
	UNKNOWN_ORDER,
	DUPLICATE_ORDER,
	UNKNOWN_ACCOUNT,
	INSUFFICIENT_BALANCE,
	ACCOUNT_INACTIVE,
	ACCOUNT_CANNOT_SETTLE,
	ORDER_WOULD_TRIGGER_IMMEDIATELY
}