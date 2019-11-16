package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

import java.util.*;

public class SimulatedExchange implements Exchange<Candle> {

	private final AccountManager account;
	private final Map<String, SymbolInformation> symbolInformation = new TreeMap<>();

	public SimulatedExchange(AccountManager account) {
		this.account = account;
	}

	@Override
	public Candle getLatestTick(String symbol, TimeInterval interval) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
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
	public Map<String, Double> getLatestPrices() {
		Map<String, Double> out = new HashMap<>();
		for (TradingManager tradingManager : account.getAllTradingManagers()) {
			out.put(tradingManager.getSymbol(), tradingManager.getLatestPrice());
		}
		return out;
	}

	@Override
	public Map<String, SymbolInformation> getSymbolInformation() {
		return symbolInformation;
	}

	public void setSymbolInformation(String symbol, SymbolInformation info){
		symbolInformation.put(symbol, info);
	}

	public void setSymbolInformation(Map<String, SymbolInformation> info){
		symbolInformation.putAll(info);
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		String symbol = assetSymbol + fundSymbol;
		Trader trader = account.getTraderOf(symbol);
		if(trader == null){
			throw new IllegalStateException("Unknown symbol: " + symbol);
		}
		double price = trader.getLastClosingPrice();
		if (price == 0.0 && trader.getCandle() == null) {
			// case for simulations only, where we try to switch from one asset to another without selling then buying, to avoid paying fees twice.
			Trader assetTrader = account.getTraderOf(assetSymbol + account.getReferenceCurrencySymbol());
			if (assetTrader != null) {
				Trader fundsTrader = account.getTraderOf(fundSymbol + account.getReferenceCurrencySymbol());
				if (fundsTrader != null) {
					double assetPrice = assetTrader.getLastClosingPrice();
					double fundPrice = fundsTrader.getLastClosingPrice();
					if (fundPrice != 0.0) {
						price = assetPrice / fundPrice;
					}
				}
			}
		}
		return price;
	}

	@Override
	public TimeInterval handlePollingException(String symbol, Exception e) {
		return TimeInterval.millis(0);
	}

	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void closeLiveStream() throws Exception {
		throw new UnsupportedOperationException();
	}

//	public void setMainTradeSymbols(String... mainTradeSymbols) {
//		Collections.addAll(this.mainTradeSymbols, mainTradeSymbols);
//	}

//	@Override
//	public boolean isDirectSwitchSupported(String currentAssetSymbol, String targetAssetSymbol) {
//		return mainTradeSymbols.contains(currentAssetSymbol) || mainTradeSymbols.contains(targetAssetSymbol);
//	}


	@Override
	public ClientAccount connectToAccount(String apiKey, String secret) {
		throw new UnsupportedOperationException();
	}
}
