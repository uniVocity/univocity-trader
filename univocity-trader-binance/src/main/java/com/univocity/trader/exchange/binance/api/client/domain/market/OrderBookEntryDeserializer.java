package com.univocity.trader.exchange.binance.api.client.domain.market;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;

/**
 * Custom deserializer for an OrderBookEntry, since the API returns an array in the format [ price, qty, [] ].
 */
public class OrderBookEntryDeserializer extends JsonDeserializer<OrderBookEntry> {

	@Override
	public OrderBookEntry deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
		ObjectCodec oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);
		final String price = node.get(0).asText();
		final String qty = node.get(1).asText();

		OrderBookEntry orderBookEntry = new OrderBookEntry();
		orderBookEntry.setPrice(price);
		orderBookEntry.setQty(qty);
		return orderBookEntry;
	}
}
