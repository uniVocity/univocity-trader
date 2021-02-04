package com.univocity.trader.exchange.binance.futures.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import com.univocity.trader.exchange.binance.futures.model.market.Candlestick;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An interval candlestick for a symbol providing informations on price that can be used to produce candlestick charts.
 */
@JsonDeserialize(using = CandlestickEventDeserializer.class)
@JsonSerialize(using = CandlestickEventSerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandlestickEvent extends Candlestick {

	private String eventType;

	private long eventTime;

	private String symbol;

	private String intervalId;

	private Long firstTradeId;

	private Long lastTradeId;

	private Boolean isBarFinal;

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

	public String getIntervalId() {
		return intervalId;
	}

	public void setIntervalId(String intervalId) {
		this.intervalId = intervalId;
	}

	public Long getFirstTradeId() {
		return firstTradeId;
	}

	public void setFirstTradeId(Long firstTradeId) {
		this.firstTradeId = firstTradeId;
	}

	public Long getLastTradeId() {
		return lastTradeId;
	}

	public void setLastTradeId(Long lastTradeId) {
		this.lastTradeId = lastTradeId;
	}

	public Boolean getBarFinal() {
		return isBarFinal;
	}

	public void setBarFinal(Boolean barFinal) {
		isBarFinal = barFinal;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("eventType", eventType)
				.append("eventTime", eventTime)
				.append("symbol", symbol)
				.append("openTime", openTime)
				.append("open", open)
				.append("high", high)
				.append("low", low)
				.append("close", close)
				.append("volume", volume)
				.append("closeTime", closeTime)
				.append("intervalId", intervalId)
				.append("firstTradeId", firstTradeId)
				.append("lastTradeId", lastTradeId)
				.append("quoteAssetVolume", quoteAssetVolume)
				.append("numberOfTrades", numberOfTrades)
				.append("takerBuyBaseAssetVolume", takerBuyBaseAssetVolume)
				.append("takerBuyQuoteAssetVolume", takerBuyQuoteAssetVolume)
				.append("isBarFinal", isBarFinal)
				.toString();
	}
}