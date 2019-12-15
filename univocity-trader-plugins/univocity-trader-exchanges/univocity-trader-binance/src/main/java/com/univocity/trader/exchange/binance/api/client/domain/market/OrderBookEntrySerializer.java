package com.univocity.trader.exchange.binance.api.client.domain.market;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;

/**
 * Custom serializer for an OrderBookEntry.
 */
public class OrderBookEntrySerializer extends JsonSerializer<OrderBookEntry> {

	@Override
	public void serialize(OrderBookEntry orderBookEntry, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStartArray();
		gen.writeString(orderBookEntry.getPrice());
		gen.writeString(orderBookEntry.getQty());
		gen.writeEndArray();
	}
}
