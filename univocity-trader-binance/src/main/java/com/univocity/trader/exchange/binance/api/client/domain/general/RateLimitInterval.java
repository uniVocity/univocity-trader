package com.univocity.trader.exchange.binance.api.client.domain.general;

import com.fasterxml.jackson.annotation.*;

/**
 * Rate limit intervals.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum RateLimitInterval {
	SECOND,
	MINUTE,
	DAY
}