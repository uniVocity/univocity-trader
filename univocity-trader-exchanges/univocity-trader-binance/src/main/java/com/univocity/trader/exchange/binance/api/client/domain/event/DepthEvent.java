package com.univocity.trader.exchange.binance.api.client.domain.event;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;

/**
 * Depth delta event for a symbol.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepthEvent {

	@JsonProperty("e")
	private String eventType;

	@JsonProperty("E")
	private long eventTime;

	@JsonProperty("s")
	private String symbol;

	@JsonProperty("U")
	private long firstUpdateId;

	/**
	 * updateId to sync up with updateid in /api/v1/depth
	 */
	@JsonProperty("u")
	private long finalUpdateId;

	/**
	 * Bid depth delta.
	 */
	@JsonProperty("b")
	private List<OrderBookEntry> bids;

	/**
	 * Ask depth delta.
	 */
	@JsonProperty("a")
	private List<OrderBookEntry> asks;

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public long getFirstUpdateId() {
		return firstUpdateId;
	}

	public void setFirstUpdateId(final long firstUpdateId) {
		this.firstUpdateId = firstUpdateId;
	}

	public long getFinalUpdateId() {
		return finalUpdateId;
	}

	public void setFinalUpdateId(long finalUpdateId) {
		this.finalUpdateId = finalUpdateId;
	}

	/**
	 * @deprecated Use {@link #getFinalUpdateId}
	 */
	@Deprecated
	public long getUpdateId() {
		return finalUpdateId;
	}

	/**
	 * @deprecated Use {@link #setFinalUpdateId}
	 */
	@Deprecated
	public void setUpdateId(long updateId) {
		this.finalUpdateId = updateId;
	}

	public List<OrderBookEntry> getBids() {
		return bids;
	}

	public void setBids(List<OrderBookEntry> bids) {
		this.bids = bids;
	}

	public List<OrderBookEntry> getAsks() {
		return asks;
	}

	public void setAsks(List<OrderBookEntry> asks) {
		this.asks = asks;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("eventType", eventType)
				.append("eventTime", eventTime)
				.append("symbol", symbol)
				.append("firstUpdateId", firstUpdateId)
				.append("finalUpdateId", finalUpdateId)
				.append("bids", bids)
				.append("asks", asks)
				.toString();
	}
}
