package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * Represents an executed trade history item.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeHistoryItem {
	/**
	 * Trade id.
	 */
	private long id;

	/**
	 * Price.
	 */
	private String price;

	/**
	 * Quantity.
	 */
	private String qty;

	/**
	 * Trade execution time.
	 */
	private long time;

	/**
	 * Is buyer maker ?
	 */
	@JsonProperty("isBuyerMaker")
	private boolean isBuyerMaker;

	/**
	 * Is best match ?
	 */
	@JsonProperty("isBestMatch")
	private boolean isBestMatch;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isBuyerMaker() {
		return isBuyerMaker;
	}

	public void setBuyerMaker(boolean buyerMaker) {
		isBuyerMaker = buyerMaker;
	}

	public boolean isBestMatch() {
		return isBestMatch;
	}

	public void setBestMatch(boolean bestMatch) {
		isBestMatch = bestMatch;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("id", id)
				.append("price", price)
				.append("qty", qty)
				.append("time", time)
				.append("isBuyerMaker", isBuyerMaker)
				.append("isBestMatch", isBestMatch)
				.toString();
	}
}
