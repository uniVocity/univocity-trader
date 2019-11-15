package com.univocity.trader.exchange.binance.api.client.domain.market;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * 24 hour price change statistics for a ticker.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerStatistics {

	/**
	 * Ticker symbol.
	 */
	private String symbol;

	/**
	 * Price change during the last 24 hours.
	 */
	private String priceChange;

	/**
	 * Price change, in percentage, during the last 24 hours.
	 */
	private String priceChangePercent;

	/**
	 * Weighted average price.
	 */
	private String weightedAvgPrice;

	/**
	 * Previous close price.
	 */
	private String prevClosePrice;

	/**
	 * Last price.
	 */
	private String lastPrice;

	/**
	 * Bid price.
	 */
	private String bidPrice;

	/**
	 * Ask price.
	 */
	private String askPrice;

	/**
	 * Open price 24 hours ago.
	 */
	private String openPrice;

	/**
	 * Highest price during the past 24 hours.
	 */
	private String highPrice;

	/**
	 * Lowest price during the past 24 hours.
	 */
	private String lowPrice;

	/**
	 * Total volume during the past 24 hours.
	 */
	private String volume;

	/**
	 * Open time.
	 */
	private long openTime;

	/**
	 * Close time.
	 */
	private long closeTime;

	/**
	 * First trade id.
	 */
	private long firstId;

	/**
	 * Last trade id.
	 */
	private long lastId;

	/**
	 * Total number of trades during the last 24 hours.
	 */
	private long count;

	public String getPriceChange() {
		return priceChange;
	}

	public void setPriceChange(String priceChange) {
		this.priceChange = priceChange;
	}

	public String getPriceChangePercent() {
		return priceChangePercent;
	}

	public void setPriceChangePercent(String priceChangePercent) {
		this.priceChangePercent = priceChangePercent;
	}

	public String getWeightedAvgPrice() {
		return weightedAvgPrice;
	}

	public void setWeightedAvgPrice(String weightedAvgPrice) {
		this.weightedAvgPrice = weightedAvgPrice;
	}

	public String getPrevClosePrice() {
		return prevClosePrice;
	}

	public void setPrevClosePrice(String prevClosePrice) {
		this.prevClosePrice = prevClosePrice;
	}

	public String getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(String lastPrice) {
		this.lastPrice = lastPrice;
	}

	public String getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(String bidPrice) {
		this.bidPrice = bidPrice;
	}

	public String getAskPrice() {
		return askPrice;
	}

	public void setAskPrice(String askPrice) {
		this.askPrice = askPrice;
	}

	public String getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(String openPrice) {
		this.openPrice = openPrice;
	}

	public String getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(String highPrice) {
		this.highPrice = highPrice;
	}

	public String getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(String lowPrice) {
		this.lowPrice = lowPrice;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}

	public long getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(long closeTime) {
		this.closeTime = closeTime;
	}

	public long getFirstId() {
		return firstId;
	}

	public void setFirstId(long firstId) {
		this.firstId = firstId;
	}

	public long getLastId() {
		return lastId;
	}

	public void setLastId(long lastId) {
		this.lastId = lastId;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("symbol", symbol)
				.append("priceChange", priceChange)
				.append("priceChangePercent", priceChangePercent)
				.append("weightedAvgPrice", weightedAvgPrice)
				.append("prevClosePrice", prevClosePrice)
				.append("lastPrice", lastPrice)
				.append("bidPrice", bidPrice)
				.append("askPrice", askPrice)
				.append("openPrice", openPrice)
				.append("highPrice", highPrice)
				.append("lowPrice", lowPrice)
				.append("volume", volume)
				.append("openTime", openTime)
				.append("closeTime", closeTime)
				.append("firstId", firstId)
				.append("lastId", lastId)
				.append("count", count)
				.toString();
	}
}
