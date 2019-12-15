package com.univocity.trader.exchange.binance.api.client.domain;

import com.fasterxml.jackson.annotation.*;

/**
 * Order execution type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum ExecutionType {
	NEW,
	CANCELED,
	REPLACED,
	REJECTED,
	TRADE,
	EXPIRED
}