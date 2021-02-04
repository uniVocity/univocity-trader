package com.univocity.trader.exchange.binance.futures.model.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Kline/Candlestick bars for a symbol. Klines are uniquely identified by their open time.
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder()
@JsonIgnoreProperties(ignoreUnknown = true)
public class Candlestick {

	protected Long openTime;
	protected String open;
	protected String high;
	protected String low;
	protected String close;
	protected String volume;
	protected Long closeTime;
	protected String quoteAssetVolume;
	protected Long numberOfTrades;
	protected String takerBuyBaseAssetVolume;
	protected String takerBuyQuoteAssetVolume;

	public final Long getOpenTime() {
		return openTime;
	}

	public final void setOpenTime(Long openTime) {
		this.openTime = openTime;
	}

	public final String getOpen() {
		return open;
	}

	public final void setOpen(String open) {
		this.open = open;
	}

	public final String getHigh() {
		return high;
	}

	public final void setHigh(String high) {
		this.high = high;
	}

	public final String getLow() {
		return low;
	}

	public final void setLow(String low) {
		this.low = low;
	}

	public final String getClose() {
		return close;
	}

	public final void setClose(String close) {
		this.close = close;
	}

	public final String getVolume() {
		return volume;
	}

	public final void setVolume(String volume) {
		this.volume = volume;
	}

	public final Long getCloseTime() {
		return closeTime;
	}

	public final void setCloseTime(Long closeTime) {
		this.closeTime = closeTime;
	}

	public final String getQuoteAssetVolume() {
		return quoteAssetVolume;
	}

	public final void setQuoteAssetVolume(String quoteAssetVolume) {
		this.quoteAssetVolume = quoteAssetVolume;
	}

	public final Long getNumberOfTrades() {
		return numberOfTrades;
	}

	public final void setNumberOfTrades(Long numberOfTrades) {
		this.numberOfTrades = numberOfTrades;
	}

	public final String getTakerBuyBaseAssetVolume() {
		return takerBuyBaseAssetVolume;
	}

	public final void setTakerBuyBaseAssetVolume(String takerBuyBaseAssetVolume) {
		this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
	}

	public final String getTakerBuyQuoteAssetVolume() {
		return takerBuyQuoteAssetVolume;
	}

	public final void setTakerBuyQuoteAssetVolume(String takerBuyQuoteAssetVolume) {
		this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("openTime", openTime)
				.append("open", open)
				.append("high", high)
				.append("low", low)
				.append("close", close)
				.append("volume", volume)
				.append("closeTime", closeTime)
				.append("quoteAssetVolume", quoteAssetVolume)
				.append("numberOfTrades", numberOfTrades)
				.append("takerBuyBaseAssetVolume", takerBuyBaseAssetVolume)
				.append("takerBuyQuoteAssetVolume", takerBuyQuoteAssetVolume)
				.toString();
	}
}
