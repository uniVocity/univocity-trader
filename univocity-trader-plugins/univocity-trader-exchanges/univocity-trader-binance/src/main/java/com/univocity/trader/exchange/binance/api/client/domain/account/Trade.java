package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * Represents an executed trade.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trade {

	/**
	 * Trade id.
	 */
	private Long id;

	/**
	 * Price.
	 */
	private String price;

	/**
	 * Quantity.
	 */
	private String qty;

	/**
	 * Quote quantity for the trade (price * qty).
	 */
	private String quoteQty;

	/**
	 * Commission.
	 */
	private String commission;

	/**
	 * Asset on which commission is taken
	 */
	private String commissionAsset;

	/**
	 * Trade execution time.
	 */
	private long time;

	/**
	 * The symbol of the trade.
	 */
	private String symbol;

	@JsonProperty("isBuyer")
	private boolean buyer;

	@JsonProperty("isMaker")
	private boolean maker;

	@JsonProperty("isBestMatch")
	private boolean bestMatch;

	private String orderId;

	public Long getId() {
		return id;
	}

	@JsonSetter("id")
	public void setId(Long id) {
		this.id = id;
	}

	@JsonSetter("tradeId")
	public void setTradeId(Long id) {
		if (this.id == null) {
			setId(id);
		}
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

	public String getQuoteQty() {
		return quoteQty;
	}

	public void setQuoteQty(String quoteQty) {
		this.quoteQty = quoteQty;
	}

	public String getCommission() {
		return commission;
	}

	public void setCommission(String commission) {
		this.commission = commission;
	}

	public String getCommissionAsset() {
		return commissionAsset;
	}

	public void setCommissionAsset(String commissionAsset) {
		this.commissionAsset = commissionAsset;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public boolean isBuyer() {
		return buyer;
	}

	public void setBuyer(boolean buyer) {
		this.buyer = buyer;
	}

	public boolean isMaker() {
		return maker;
	}

	public void setMaker(boolean maker) {
		this.maker = maker;
	}

	public boolean isBestMatch() {
		return bestMatch;
	}

	public void setBestMatch(boolean bestMatch) {
		this.bestMatch = bestMatch;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("id", id)
				.append("symbol", symbol)
				.append("price", price)
				.append("qty", qty)
				.append("quoteQty", quoteQty)
				.append("commission", commission)
				.append("commissionAsset", commissionAsset)
				.append("time", time)
				.append("buyer", buyer)
				.append("maker", maker)
				.append("bestMatch", bestMatch)
				.append("orderId", orderId)
				.toString();
	}

	public double getPriceAmount() {
		return Double.parseDouble(price);
	}

	public double getQtyAmount() {
		return Double.parseDouble(qty);
	}
}
