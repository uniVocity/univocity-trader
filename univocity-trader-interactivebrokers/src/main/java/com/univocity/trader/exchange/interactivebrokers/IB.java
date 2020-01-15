package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.interactivebrokers.api.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.exchange.interactivebrokers.SecurityType.*;


class IB implements Exchange<Candle, Account> {

	private final InteractiveBrokersApi api;
	private Map<String, Contract> tradedContracts;
	private Map<String, SymbolInformation> symbolInformation;

	IB() {
		this("", 7497, 0, "");
	}

	IB(String ip, int port, int clientID, String optionalCapabilities) {
		api = new InteractiveBrokersApi(ip, port, clientID, optionalCapabilities);
	}

	private void validateContracts() {
		if (tradedContracts == null || tradedContracts.isEmpty()) {
			throw new IllegalConfigurationException("No account configuration provided with one or more contracts to trade with. " +
					"Use `configure().account().tradeWith(...)` to define the contracts");
		}
	}

	@Override
	public IBAccount connectToAccount(Account account) {
		if (tradedContracts == null) {
			tradedContracts = new ConcurrentHashMap<>(account.tradedContracts());
		} else {
			tradedContracts.putAll(account.tradedContracts());
		}

		return new IBAccount(this);
	}

	@Override
	public Candle getLatestTick(String symbol, TimeInterval interval) {
		validateContracts();
		return null;
	}

	@Override
	public IncomingCandles<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		Calendar halfAnHourAgo = Calendar.getInstance();
		halfAnHourAgo.set(Calendar.MINUTE, -30);

		return getHistoricalTicks(symbol, interval, halfAnHourAgo.getTimeInMillis(), System.currentTimeMillis());
	}

	@Override
	public IncomingCandles<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		Contract contract = getContract(symbol);
		return api.loadHistoricalData(contract, startTime, endTime);
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
		//TODO
	}

	@Override
	public void closeLiveStream() throws Exception {
		api.disconnect();
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

			List<Integer> requests = new ArrayList<>();
			for (Map.Entry<String, Contract> e : tradedContracts.entrySet()) {
				requests.add(this.api.searchForContract(e.getValue(), (details) -> symbolInformation.put(e.getKey(), details)));
			}
			this.api.waitForResponses(requests);
		}

		return symbolInformation;
	}

	private Contract getContract(String symbol) {
		validateContracts();
		return tradedContracts.get(symbol);
	}

	private Contract getContract(String assetSymbol, String fundSymbol) {
		return getContract(assetSymbol + fundSymbol);
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		return 0;
	}

	@Override
	public TimeInterval handlePollingException(String symbol, Exception e) {
		return null;
	}

	//TODO: remove this once implementation is finalized
	public static void main(String... args) throws Exception {


		InteractiveBrokers.Simulator simulator = InteractiveBrokers.simulator();
		Account account = simulator.configure().account();
		account.referenceCurrency("USD");

//		account.tradeWith(FOREX, "USD", "EUR");

		account.tradeWith(FOREX, "EUR", "GBP");
		account.tradeWith(STOCKS, "GOOG", "USD").primaryExch("ISLAND");
		;

//		IB ib = new IB();
//		ib.connectToAccount(account);
//
//		System.out.println(ib.getSymbolInformation());

		simulator.backfillHistory("EURGBP", "GOOGUSD");

	}
}
