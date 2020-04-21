package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.interactivebrokers.api.*;
import com.univocity.trader.exchange.interactivebrokers.model.book.*;
import com.univocity.trader.indicators.base.*;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;


class IB implements Exchange<Candle, Account> {

	private static final Logger log = LoggerFactory.getLogger(IB.class);
	private static final double[] ZERO = new double[]{0.0};

	private InteractiveBrokersApi api;
	private Map<String, Contract> tradedContracts;
	private Map<String, TradeType> tradeTypes = new ConcurrentHashMap<>();
	private Map<String, SymbolInformation> symbolInformation;

	private Map<String, double[]> latestPrices = new ConcurrentHashMap<>();
	private Map<String, LiveIBIncomingCandles> activeStreams = new ConcurrentHashMap<>();

	IB() {
		this("", 7497, 0, "");
	}

	IB(String ip, int port, int clientID, String optionalCapabilities) {
		api = new InteractiveBrokersApi(ip, port, clientID, optionalCapabilities, this::reconnectApi);
	}

	private void reconnectApi() {
		api = (InteractiveBrokersApi) InteractiveBrokersApi.reconnect(api);
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

		tradeTypes.putAll(account.tradeTypes());

		return new IBAccount(this, account);
	}

	@Override
	public IBIncomingCandles getLatestTicks(String symbol, TimeInterval interval) {
		Calendar halfAnHourAgo = Calendar.getInstance();
		halfAnHourAgo.set(Calendar.MINUTE, -30);

		return getHistoricalTicks(symbol, interval, halfAnHourAgo.getTimeInMillis(), System.currentTimeMillis());
	}

	@Override
	public IBIncomingCandles getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		Contract contract = getContract(symbol);
		TradeType tradeType = tradeTypes.get(symbol);
		return api.loadHistoricalData(contract, startTime, endTime, interval, tradeType);
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

		for (String symbol : symbols.split(",")) {
			Contract contract = getContract(symbol.toUpperCase());
			SecurityType securityType = SecurityType.fromSecurityCode(contract.getSecType());

			//TODO: allow this to be configurable
			TradeType tradeType = securityType.defaultTradeType();
			TickType tickType = securityType.defaultTickType();
			Types.WhatToShow whatToShow = null; //not used yet

			LiveIBIncomingCandles feed = this.api.openFeed(symbol, contract, tickInterval, tradeType, tickType, whatToShow, consumer);
			activeStreams.put(symbol, feed);
		}
	}

	@Override
	public void closeLiveStream() throws Exception {
		api.disconnect();
	}

	@Override
	public Map<String, double[]> getLatestPrices() {
		for (LiveIBIncomingCandles live : activeStreams.values()) {
			latestPrices.computeIfAbsent(live.symbol, s -> new double[]{0.0})[0] = live.latestPrice();
		}
		return latestPrices;
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

	public ConcurrentHashMap<String, Balance> getAccountBalances(String referenceCurrency) {
		ConcurrentHashMap<String, Balance> out = new ConcurrentHashMap<>();
//		int requestId = this.api.loadAccountBalances(referenceCurrency, (balance -> out.put(balance.getSymbol(), balance)));
//		this.api.waitForResponse(requestId);

		int requestId = this.api.loadAccountPositions((balance -> out.put(balance.getSymbol(), balance)));
		this.api.waitForResponse(requestId);
		return out;
	}

	public Candle getLatestTick(String symbol, TimeInterval interval) {
		LiveIBIncomingCandles stream = activeStreams.get(symbol);
		if(stream == null){
			return null;
		}
		return stream.latestCandle;
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		return latestPrices.getOrDefault(assetSymbol + fundSymbol, ZERO)[0];
	}

	@Override
	public int historicalCandleCountLimit() {
		return 1_000;
	}

	@Override
	public long timeToWaitPerRequest() {
		return 10_000L;
	}

	OrderBook getOrderBook(IBAccount account, boolean smartDepth, String symbol, int depth){
		OrderBook out = new OrderBook(account, symbol, depth);

		Contract contract = getContract(symbol);

		int requestId = this.api.populateTradingBook(symbol, smartDepth, contract, depth, t -> {
			BookEntry[] asks = t.asks();
			for(int i = 0; i < asks.length; i++){
				out.addAsk(asks[i].price, asks[i].quantity);
			}
			BookEntry[] bids = t.bids();
			for(int i = 0; i < bids.length; i++){
				out.addBid(bids[i].price, bids[i].quantity);
			}
		});
		//this.api.waitFor(requestId, );

		return out;
	}
}
