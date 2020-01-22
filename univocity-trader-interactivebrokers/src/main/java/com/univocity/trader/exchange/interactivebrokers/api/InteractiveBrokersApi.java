package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.exchange.interactivebrokers.*;
import com.univocity.trader.indicators.base.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.exchange.interactivebrokers.TradeType.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class InteractiveBrokersApi extends IBRequests {

	private static final Map<TimeUnit, Set<Integer>> validBarSizes = new ConcurrentHashMap<>();

	static {
		validBarSizes.put(SECONDS, Set.of(1, 5, 10, 15, 30));
		validBarSizes.put(TimeUnit.MINUTES, Set.of(1, 2, 3, 5, 10, 15, 20, 30));
		validBarSizes.put(TimeUnit.HOURS, Set.of(1, 2, 3, 4, 8));
		validBarSizes.put(TimeUnit.DAYS, Set.of(1, 7, 30));
	}


	public InteractiveBrokersApi(String ip, int port, int clientID, String optionalCapabilities, Runnable reconnectionProcess) {
		super(ip, port, clientID, optionalCapabilities, reconnectionProcess);
	}

	public int searchForContract(Contract query, Consumer<SymbolInformation> resultConsumer) {
		return submitRequest("Searching for contract\n" + query, resultConsumer,
				(reqId) -> client.reqContractDetails(reqId, query));
	}

	public int searchForContracts(String symbolSearch, Consumer<SymbolInformation> resultConsumer) {
		return submitRequest("Searching for contracts matching '" + symbolSearch + "'", resultConsumer,
				(reqId) -> client.reqMatchingSymbols(reqId, symbolSearch));
	}

	public IBIncomingCandles loadHistoricalData(Contract contract, long startTime, long endTime, TimeInterval interval, TradeType tradeType) {
		if (interval.ms <= 1) {
			return requestHandler.openFeed(
					(consumer) -> loadHistoricalData(contract, startTime, endTime, interval, tradeType, consumer));
		} else {
			return requestHandler.openFeed(
					(consumer) -> loadHistoricalData(contract, startTime, endTime, interval, tradeType, consumer),
					(requestId) -> client.cancelHistoricalData(requestId));
		}
	}

	private String getBarSizeString(TimeInterval interval) {
		if (interval.unit == DAYS) {
			switch ((int) interval.duration) {
				case 1:
					return "1 day";
				case 7:
					return "1 week";
				case 30:
					return "1 month";
			}
		} else if (validBarSizes.getOrDefault(interval.unit, Collections.emptySet()).contains((int) interval.duration)) {
			String str = "";
			switch (interval.unit) {
				case HOURS:
					str = "hour";
					break;
				case MINUTES:
					str = "min";
					break;
				case SECONDS:
					str = "sec";
					break;
			}

			if (interval.duration > 1) {
				str = str + "s";
			}
			return interval.duration + " " + str;
		}
		throw new IllegalArgumentException("Can't use interval '" + interval + "'. Only the following candle intervals are allowed: " + validBarSizes);
	}

	private String toDurationString(long startTime, long endTime) {
		LocalDate start = LocalDate.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
		LocalDate end = LocalDate.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

		Period diff = Period.between(start, end);
		if (diff.getYears() > 0) {
			return diff.getYears() + " Y"; //years
		}
		if (diff.getMonths() > 0) {
			return diff.getMonths() + " M"; //months
		}
		if (diff.getDays() > 0) {
			if (diff.getDays() % 7 == 0) {
				return (diff.getDays() / 7) + " W"; //weeks
			} else {
				return diff.getDays() + " D"; //days
			}
		}
		return ((endTime - startTime) / 1000) + " S"; //seconds
	}

	private int loadHistoricalData(Contract contract, long startTime, long endTime, TimeInterval interval, TradeType tradeType, Consumer<Candle> candleConsumer) {
		String durationStr = toDurationString(startTime, endTime);


		boolean requestTicks = interval.ms <= 1;

		String candleStr = requestTicks ? "" : getBarSizeString(interval);
		String description = "Loading historical " + tradeType + " " + candleStr + " " + (requestTicks ? "ticks" : "candles") + " of " + contract.symbol() + contract.currency() + " data";
		String formattedEnd;
		String formattedStart;
		if (!requestTicks && tradeType == ADJUSTED_LAST) {
			formattedEnd = ""; // formatted end not supported with ADJUSTED LAST
			formattedStart = requestHandler.getFormattedDateTime(startTime);
			description += " since " + formattedStart;
		} else {
			formattedEnd = requestHandler.getFormattedDateTime(endTime);
			if (requestTicks) {
				formattedStart = "";
				description += "from " + formattedEnd + " and back";
			} else {
				formattedStart = requestHandler.getFormattedDateTime(startTime);
				description += " between " + formattedStart + " and " + formattedEnd;
			}
		}

		Consumer<Integer> request;
		if (requestTicks) {
			// Data is returned to the functions
			// IBApi.EWrapper.historicalTicks (for whatToShow=MIDPOINT),
			// IBApi.EWrapper.historicalTicksBidAsk (for whatToShow=BID_ASK),
			// IBApi.EWrapper.historicalTicksLast for (for whatToShow=TRADES)
			// depending on the type of data requested.
			request = (reqId) ->
					client.reqHistoricalTicks(reqId, contract, formattedStart, formattedEnd, 1000, tradeType.toString(), 1, true, null);
		} else {
			request = (reqId) ->
					client.reqHistoricalData(reqId, contract, formattedEnd, durationStr, candleStr, tradeType.toString(), 1, 1, false, null);
		}


		return submitRequest(description, candleConsumer, request);
	}

	@Override
	IBRequests newInstance(IBRequests old) {
		return new InteractiveBrokersApi(old.ip, old.port, old.clientID, old.optionalCapabilities, old.requestHandler.reconnectProcess);
	}
}
