package com.univocity.trader.exchange.binance.api.client.domain.general;

import com.fasterxml.jackson.annotation.*;

/**
 * Rate limiters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum RateLimitType {
	REQUEST_WEIGHT,
	ORDERS
}
