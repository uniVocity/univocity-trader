package com.univocity.trader;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import java.time.*;
import java.util.*;

public interface ExchangeApi<T> {

	T getLatestTick(String symbol, TimeInterval interval);

	List<T> getLatestTicks(String symbol, TimeInterval interval);

	List<T> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime);

	Candle generateCandle(T realTimeTick);

	PreciseCandle generatePreciseCandle(T realTimeTick);

	TimeInterval handleException(String action, String symbol, Exception e);

	void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<T> consumer);

	void closeLiveStream() throws Exception;

	Map<String, Double> getLatestPrices();

	Map<String, SymbolInformation> getSymbolInformation();

	double getLatestPrice(String assetSymbol, String fundSymbol);

	ClientAccountApi connectToAccount(String apiKey, String secret);

//	boolean isDirectSwitchSupported(String currentAssetSymbol, String targetAssetSymbol);
}
