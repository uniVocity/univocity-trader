package com.univocity.trader.exchange.binance.api.client.domain.event;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * User data update event which can be of two types:
 * 1) outboundAccountInfo, whenever there is a change in the account (e.g. balance of an asset)
 * 2) outboundAccountPosition, the change in account balances caused by an event.
 * 3) executionReport, whenever there is a trade or an order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = UserDataUpdateEventDeserializer.class)
public class UserDataUpdateEvent {

	private UserDataUpdateEventType eventType;

	private long eventTime;

	private AccountUpdateEvent accountUpdateEvent;

	private OrderTradeUpdateEvent orderTradeUpdateEvent;

	public UserDataUpdateEventType getEventType() {
		return eventType;
	}

	public void setEventType(UserDataUpdateEventType eventType) {
		this.eventType = eventType;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	public AccountUpdateEvent getAccountUpdateEvent() {
		return accountUpdateEvent;
	}

	public void setAccountUpdateEvent(AccountUpdateEvent accountUpdateEvent) {
		this.accountUpdateEvent = accountUpdateEvent;
	}

	public OrderTradeUpdateEvent getOrderTradeUpdateEvent() {
		return orderTradeUpdateEvent;
	}

	public void setOrderTradeUpdateEvent(OrderTradeUpdateEvent orderTradeUpdateEvent) {
		this.orderTradeUpdateEvent = orderTradeUpdateEvent;
	}

	@Override
	public String toString() {
		ToStringBuilder sb = new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("eventType", eventType)
				.append("eventTime", eventTime);
		if (eventType == UserDataUpdateEventType.ACCOUNT_UPDATE) {
			sb.append("accountUpdateEvent", accountUpdateEvent);
		} else if (eventType == UserDataUpdateEventType.ACCOUNT_POSITION_UPDATE) {
			sb.append("accountPositionUpdateEvent", accountUpdateEvent);
		} else {
			sb.append("orderTradeUpdateEvent", orderTradeUpdateEvent);
		}
		return sb.toString();
	}

	public enum UserDataUpdateEventType {
		ACCOUNT_UPDATE("outboundAccountInfo"),
		ACCOUNT_POSITION_UPDATE("outboundAccountPosition"),
		ORDER_TRADE_UPDATE("executionReport");

		private final String eventTypeId;

		UserDataUpdateEventType(String eventTypeId) {
			this.eventTypeId = eventTypeId;
		}

		public String getEventTypeId() {
			return eventTypeId;
		}

		public static UserDataUpdateEventType fromEventTypeId(String eventTypeId) {
			if (ACCOUNT_UPDATE.eventTypeId.equals(eventTypeId)) {
				return ACCOUNT_UPDATE;
			} else if (ORDER_TRADE_UPDATE.eventTypeId.equals(eventTypeId)) {
				return ORDER_TRADE_UPDATE;
			} else if (ACCOUNT_POSITION_UPDATE.eventTypeId.equals(eventTypeId)) {
				return ACCOUNT_POSITION_UPDATE;
			}
			throw new IllegalArgumentException("Unrecognized user data update event type id: " + eventTypeId);
		}
	}
}
