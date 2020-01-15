package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class InteractiveBrokersApi extends IBRequests {

	public InteractiveBrokersApi(String ip, int port, int clientID, String optionalCapabilities) {
		super(ip, port, clientID, optionalCapabilities);
	}

	public int searchForContract(Contract query, Consumer<SymbolInformation> resultConsumer) {
		return submitRequest("Searching for contract\n" + query, resultConsumer,
				(reqId) -> client.reqContractDetails(reqId, query));
	}

	public int searchForContracts(String symbolSearch, Consumer<SymbolInformation> resultConsumer) {
		return submitRequest("Searching for contracts matching '" + symbolSearch + "'", resultConsumer,
				(reqId) -> client.reqMatchingSymbols(reqId, symbolSearch));
	}

	public IncomingCandles<Candle> loadHistoricalData(Contract contract, long startTime, long endTime) {
		return requestHandler.openFeed((consumer)-> loadHistoricalData(contract, startTime, endTime, consumer));
	}

	private int loadHistoricalData(Contract contract, long startTime, long endTime, Consumer<Candle> candleConsumer) {
		String formattedStart = requestHandler.getFormattedDateTime(startTime);
		String formattedEnd = requestHandler.getFormattedDateTime(endTime);

		String description = "Loading " + contract.symbol() + contract.currency() + " historical data between " + formattedStart + " and " + formattedEnd;

		return submitRequest(description, candleConsumer,
				(reqId) -> client.reqHistoricalData(reqId, contract, formattedEnd, "1 M", "1 day", "MIDPOINT", 1, 1, false, null));

	}
}
