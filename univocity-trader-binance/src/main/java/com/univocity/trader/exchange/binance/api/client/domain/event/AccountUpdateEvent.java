package com.univocity.trader.exchange.binance.api.client.domain.event;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.domain.account.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;

/**
 * Account update event which will reflect the current position/balances of the account.
 *
 * This event is embedded as part of a user data update event.
 *
 * @see UserDataUpdateEvent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountUpdateEvent {

	@JsonProperty("e")
	private String eventType;

	@JsonProperty("E")
	private long eventTime;

	@JsonProperty("B")
	@JsonDeserialize(contentUsing = AssetBalanceDeserializer.class)
	private List<AssetBalance> balances;

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

	public List<AssetBalance> getBalances() {
		return balances;
	}

	public void setBalances(List<AssetBalance> balances) {
		this.balances = balances;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("eventType", eventType)
				.append("eventTime", eventTime)
				.append("balances", balances)
				.toString();
	}
}
