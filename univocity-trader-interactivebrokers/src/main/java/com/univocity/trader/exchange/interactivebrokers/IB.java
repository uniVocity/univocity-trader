package com.univocity.trader.exchange.interactivebrokers;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.slf4j.*;

import java.util.*;


class IB implements Exchange<Candle, Account> {

	private static final Logger log = LoggerFactory.getLogger(IB.class);

	IB(){

	}

	@Override
	public InteractiveBrokersAccount connectToAccount(Account clientConfiguration) {
		return new InteractiveBrokersAccount(this);
	}

	@Override
	public Candle getLatestTick(String symbol, TimeInterval interval) {
		return null;
	}

	@Override
	public List<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		return null;
	}

	@Override
	public List<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		return null;
	}

	@Override
	public Candle generateCandle(Candle exchangeCandle) {
		return null;
	}

	@Override
	public PreciseCandle generatePreciseCandle(Candle exchangeCandle) {
		return null;
	}

	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {

	}

	@Override
	public void closeLiveStream() throws Exception {

	}

	@Override
	public Map<String, Double> getLatestPrices() {
		return null;
	}

	@Override
	public Map<String, SymbolInformation> getSymbolInformation() {
		return null;
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		return 0;
	}

	@Override
	public TimeInterval handlePollingException(String symbol, Exception e) {
		return null;
	}
}
