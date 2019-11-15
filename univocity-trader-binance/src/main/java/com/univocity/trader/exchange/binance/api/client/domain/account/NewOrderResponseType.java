package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.fasterxml.jackson.annotation.*;

/**
 * Desired response type of NewOrder requests.
 *
 * @see NewOrderResponse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum NewOrderResponseType {
	ACK,
	RESULT,
	FULL
}

