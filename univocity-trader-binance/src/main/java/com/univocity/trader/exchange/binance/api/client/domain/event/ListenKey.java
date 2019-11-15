package com.univocity.trader.exchange.binance.api.client.domain.event;

import com.fasterxml.jackson.annotation.*;

/**
 * Dummy type to wrap a listen key from a server response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListenKey {

	private String listenKey;

	public String getListenKey() {
		return listenKey;
	}

	public void setListenKey(String listenKey) {
		this.listenKey = listenKey;
	}

	@Override
	public String toString() {
		return listenKey;
	}
}
