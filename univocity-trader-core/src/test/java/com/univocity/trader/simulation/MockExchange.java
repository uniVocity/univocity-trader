package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public class MockExchange implements Exchange<Candle, SimulationAccount> {

	private final Map<String, double[]> latestPrices = new ConcurrentHashMap<>();
	boolean running = false;
	private Map<String, List<Candle>> candles;

	public MockExchange() {

	}

	public MockExchange(Map<String, List<Candle>> candles) {
		this.candles = candles;
	}

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
	public PreciseCandle generatePreciseCandle(Candle exchangeCandle) {
		return new PreciseCandle(exchangeCandle);
	}

	private Candle latestCandle;

	@Override
	public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candle> consumer) {
		running = true;
		String[] pairs = symbols.split(",");
		new Thread(() -> {
			Thread.currentThread().setName("Live stream for: " + symbols);
			long time = LocalDateTime.of(2010, Month.JANUARY, 1, 10, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
			;
			while (running) {
				boolean empty = true;
				for (String pair : pairs) {
					Candle candle = null;
					if (candles != null) {
						List<Candle> list = candles.get(pair.toUpperCase());
						if (!list.isEmpty()) {
							latestCandle = candle = list.remove(0);
							empty = false;
						}
					} else {
						double close = Math.random() * 10.0;
						candle = new Candle(time, time + MINUTE.ms, close, close, close, close, 100);
						System.out.println(candle.close);
					}
					if (candle != null) {
						consumer.tickReceived(pair, candle);
					}
				}

				if (empty && candles != null) {
					running = false;
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
		if (latestCandle == null) {
			return Math.random() * 10.0;
		} else {
			return latestCandle.close;
		}
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

		private DatabaseCandleRepository noop;

		private Trader(Map<String, List<Candle>> candles) {
			super(new MockExchange(candles), new Configuration());

			DatabaseConfiguration cfg = new DatabaseConfiguration();
			noop = new DatabaseCandleRepository(cfg) {
				@Override
				public boolean addToHistory(String symbol, PreciseCandle tick, boolean initializing) {
					return true;
				}
			};

			configure().updateHistoryBeforeLiveTrading(false);
			configure().pollCandles(false);
		}

		@Override
		public DatabaseCandleRepository candleRepository() {
			return noop;
		}
	}

	public static Trader trader(Map<String, List<Candle>> candles) {
		return new Trader(candles);
	}

	public static Trader trader() {
		return new Trader(null);
	}
}
