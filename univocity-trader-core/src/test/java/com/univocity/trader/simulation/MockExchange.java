package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public class MockExchange implements Exchange<Candle, SimulationAccount> {

	private final Map<String, double[]> latestPrices = new ConcurrentHashMap<>();
	boolean running = false;

	@Override
	public IncomingCandles<Candle> getLatestTicks(String symbol, TimeInterval interval) {
		var out = new IncomingCandles<Candle>(1);
		out.stopProducing();
		return out;
	}

	@Override
	public IncomingCandles<Candle> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
		return getLatestTicks(null, null);
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
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {
		running = true;
		String[] pairs = symbols.split(",");
		new Thread(() -> {
			Thread.currentThread().setName("Live stream for: " + symbols);
			long time = 0;
			while (running) {
				for (String pair : pairs) {
					double close = Math.random() * 10.0;
					consumer.tickReceived(pair, new Candle(time, time + MINUTE.ms, close, close, close, close, 100));

				}
				time += MINUTE.ms;

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}).start();
	}

	@Override
	public void closeLiveStream() throws Exception {
		running = false;
	}

	@Override
	public Map<String, double[]> getLatestPrices() {
		latestPrices.entrySet().forEach(e -> e.getValue()[0] = Math.random() * 10);
		return latestPrices;
	}

	@Override
	public Map<String, SymbolInformation> getSymbolInformation() {
		return null;
	}

	@Override
	public double getLatestPrice(String assetSymbol, String fundSymbol) {
		return Math.random() * 10.0;
	}

	@Override
	public ClientAccount connectToAccount(SimulationAccount accountConfiguration) {
		return new MockClientAccount(accountConfiguration);
	}

	public static final class Configuration extends com.univocity.trader.config.Configuration<Configuration, SimulationAccount> {
		private Configuration() {
			super("mock.properties");
		}

		@Override
		protected SimulationAccount newAccountConfiguration(String id) {
			SimulationAccount out = new SimulationAccount(id);
			return out;
		}
	}

	public static final class Trader extends LiveTrader<Candle, Configuration, SimulationAccount> {

		private CandleRepository noop;

		private Trader() {
			super(new MockExchange(), new Configuration());

			DatabaseConfiguration cfg = new DatabaseConfiguration();
			noop = new CandleRepository(cfg) {
				@Override
				public boolean addToHistory(String symbol, PreciseCandle tick, boolean initializing) {
					return true;
				}
			};

			configure().updateHistoryBeforeLiveTrading(false);
			configure().pollCandles(false);
		}

		@Override
		public CandleRepository candleRepository() {
			return noop;
		}

		protected AccountManager createAccountManager(ClientAccount clientAccount, SimulationAccount account) {
			SimulatedAccountManager out = new SimulatedAccountManager((MockClientAccount) clientAccount, account, SimpleTradingFees.percentage(0.0));
			((MockClientAccount)clientAccount).accountManager = out;
			out.setAmount("USDT", 100);
			return out;
		}
	}

	public static Trader trader() {
		return new Trader();
	}
}
