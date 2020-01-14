package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.interactivebrokers.api.*;
import com.univocity.trader.indicators.base.*;

import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.exchange.interactivebrokers.SecurityType.*;


class IB implements Exchange<Candle, Account> {

	private final IBRequests ib;
	private Map<String, Contract> tradedContracts;
	private Map<String, SymbolInformation> symbolInformation;

	IB() {
		this("", 7497, 0, "");
	}

	IB(String ip, int port, int clientID, String optionalCapabilities) {
		ib = new IBRequests(ip, port, clientID, optionalCapabilities);
	}

	private void validateContracts() {
		if (tradedContracts.isEmpty()) {
			throw new IllegalConfigurationException("No account configuration provided with one or more contracts to trade with. " +
					"Use `configure().account().tradeWith(...)` to define the contracts");
		}
	}

	@Override
	public IBAccount connectToAccount(Account account) {
		tradedContracts = account.tradedContracts();
		return new IBAccount(this);
	}

	@Override
	public Candle getLatestTick(String symbol, TimeInterval interval) {
		validateContracts();
		return null;
	}

	@Override
	public List<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		validateContracts();
		return null;
	}

	@Override
	public List<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		validateContracts();
		return null;
	}

	@Override
	public Candle generateCandle(Candle exchangeCandle) {
		return exchangeCandle;
	}

	@Override
	public PreciseCandle generatePreciseCandle(Candle exchangeCandle) {
		return new PreciseCandle(exchangeCandle);
	}

	@Override
	public synchronized void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {
		validateContracts();
	}

	@Override
	public void closeLiveStream() throws Exception {
		validateContracts();
		ib.disconnect();
	}

	@Override
	public Map<String, Double> getLatestPrices() {
		validateContracts();
		return null;
	}

	@Override
	public synchronized Map<String, SymbolInformation> getSymbolInformation() {
		if (symbolInformation == null) {
			validateContracts();
			symbolInformation = new ConcurrentHashMap<>();

			for (Map.Entry<String, Contract> e : tradedContracts.entrySet()) {
				ib.searchForContract(e.getValue(), (details) -> symbolInformation.put(e.getKey(), details));
			}
		}

		return symbolInformation;
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		validateContracts();
		return 0;
	}

	@Override
	public TimeInterval handlePollingException(String symbol, Exception e) {
		return null;
	}

	//TODO: remove this once implementation is finalized
	public static void main(String... args) throws Exception {
		InteractiveBrokers.Trader trader = InteractiveBrokers.trader();
		Account account = trader.configure().account();

		account.tradeWith(FOREX, "USD", "EUR");
		account.tradeWith(STOCKS, "GOOG", "USD");

		trader.run();
	}
}
