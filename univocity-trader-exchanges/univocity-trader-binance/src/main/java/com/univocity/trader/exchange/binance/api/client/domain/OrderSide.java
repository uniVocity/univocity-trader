package com.univocity.trader.exchange.binance.api.client.domain;

import com.fasterxml.jackson.annotation.*;

/**
 * Buy/Sell order side.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum OrderSide {
	BUY,
	SELL
}
