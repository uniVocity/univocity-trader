package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

import java.util.*;

public final class SimulatedExchange implements Exchange<Candle, SimulatedClientConfiguration> {

	private final AccountManager account;
	private final Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	private double[][] prices;

	public SimulatedExchange(AccountManager account) {
		this.account = account;
	}

	@Override
	public IncomingCandles<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IncomingCandles<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Candle generateCandle(Candle exchangeCandle) {
		return exchangeCandle;
	}

	@Override
	public PreciseCandle generatePreciseCandle(Candle exchangeCandle) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, double[]> getLatestPrices() {
		return account.getLatestPrices();
	}

	@Override
	public Map<String, SymbolInformation> getSymbolInformation() {
		return symbolInformation;
	}

	public void setSymbolInformation(String symbol, SymbolInformation info) {
		symbolInformation.put(symbol, info);
	}

	public void setSymbolInformation(Map<String, SymbolInformation> info) {
		symbolInformation.putAll(info);
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		return account.getLatestPrice(assetSymbol+fundSymbol);
	}

	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeLiveStream() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClientAccount connectToAccount(SimulatedClientConfiguration clientConfiguration) {
		throw new UnsupportedOperationException();
	}
}

class SimulatedClientConfiguration extends AccountConfiguration<SimulatedClientConfiguration> {
	public SimulatedClientConfiguration(String id) {
		super(id);
	}
}