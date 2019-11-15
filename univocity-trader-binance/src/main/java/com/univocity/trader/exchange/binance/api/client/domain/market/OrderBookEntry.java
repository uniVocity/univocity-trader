package com.univocity.trader.exchange.binance.api.client.domain.market;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import org.apache.commons.lang3.builder.*;

/**
 * An order book entry consisting of price and quantity.
 */
@JsonDeserialize(using = OrderBookEntryDeserializer.class)
@JsonSerialize(using = OrderBookEntrySerializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBookEntry {

	private String price;
	private String qty;

	public String getPrice() {
		return price;
	}

	public double getPriceAmount() {
		return Double.parseDouble(price);
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

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("price", price)
				.append("qty", qty)
				.toString();
	}
}
