package com.univocity.trader.exchange.binance.api.client.domain;

import com.fasterxml.jackson.annotation.*;

/**
 * Type of order to submit to the system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum OrderType {
	LIMIT,
	MARKET,
	STOP_LOSS,
	STOP_LOSS_LIMIT,
	TAKE_PROFIT,
	TAKE_PROFIT_LIMIT,
	LIMIT_MAKER
}
