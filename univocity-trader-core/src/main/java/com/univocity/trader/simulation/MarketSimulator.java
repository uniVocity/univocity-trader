package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MarketSimulator extends AbstractSimulator {

	private static final Logger log = LoggerFactory.getLogger(MarketSimulator.class);

	private final Map<String, Engine> symbolHandlers = new HashMap<>();
	private int activeQueryLimit = 15;
	private boolean cachingEnabled = false;

	public MarketSimulator(String referenceCurrency) {
		super(referenceCurrency);
	}

	public boolean isCachingEnabled() {
		return cachingEnabled;
	}

	public void setCachingEnabled(boolean cachingEnabled) {
		this.cachingEnabled = cachingEnabled;
	}

	public int getActiveQueryLimit() {
		return activeQueryLimit;
	}

	public void setActiveQueryLimit(int activeQueryLimit) {
		this.activeQueryLimit = activeQueryLimit;
	}

	public void run() {
		run(Parameters.NULL);
	}

	public void run(Parameters parameters) {
		AccountManager account = getAccount();
		symbolHandlers.clear();

		for (String[] pair : account.getTradedPairs()) {
			String assetSymbol = pair[0];
			String fundSymbol = pair[1];

			SimulatedExchangeApi api = new SimulatedExchangeApi(account);
			api.setSymbolInformation(this.symbolInformation);
			SymbolPriceDetails symbolPriceDetails = new SymbolPriceDetails(api);
//			api.setMainTradeSymbols(mainTradeSymbols);
			TradingManager tradingManager = new TradingManager(api, symbolPriceDetails, account, listeners, assetSymbol, fundSymbol, parameters);

			Engine engine = new Engine(tradingManager, strategies, monitors, parameters);
			symbolHandlers.put(engine.getSymbol(), engine);
		}

		ConcurrentHashMap<String, Enumeration<Candle>> markets = new ConcurrentHashMap<>();
		HashMap<String, Candle> pending = new HashMap<>();

		LocalDateTime start = getSimulationStart();
		LocalDateTime end = getSimulationEnd();
		final long startTime = getStartTime();
		final long endTime = getEndTime();


		int activeQueries = 0;
		// load everything from each symbol in separate thread, store the futures in the enumeration, leave it running
		Map<String, CompletableFuture<Enumeration<Candle>>> futures = new HashMap<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		// loads the values from the database, stores open results in memory to iterate through, contains the candles
		for (Engine engine : symbolHandlers.values()) {
			activeQueries++;
			boolean loadAllDataFirst = cachingEnabled || activeQueries > activeQueryLimit;

			futures.put(engine.getSymbol(), CompletableFuture.supplyAsync(
					() -> CandleRepository.iterate(engine.getSymbol(), start.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC), loadAllDataFirst), executor)
			);
		}

		//
		futures.forEach((symbol, candles) -> {
			try {
				markets.put(symbol, candles.get());
			} catch (Exception e) {
				log.error("Error querying " + symbol + " candles from database", e);
			}
		});

		executor.shutdown();

//		Map<String, Long> counts = new TreeMap<>();
		// from start time of simulation, will tick the clock 1m forward, grab all of the loaded candles at that time,
		for (long clock = startTime; clock <= endTime; clock += MINUTE.ms) {
			for (Map.Entry<String, Enumeration<Candle>> e : markets.entrySet()) {
				String symbol = e.getKey();
				Enumeration<Candle> it = e.getValue();
				Candle candle = pending.get(symbol);
				if (candle != null) {
					if (candle.openTime + 1 >= clock && candle.openTime <= clock + MINUTE.ms - 1) {
					    // call process for that candle in the future
						symbolHandlers.get(symbol).process(candle, false);
						// if the candle is not in the date, it will store the candle to use later
//						counts.compute(symbol, (p, c) -> c == null ? 1 : c + 1);
						pending.remove(symbol);
						if (it.hasMoreElements()) {
							Candle next = it.nextElement();
							if (next != null) {
								pending.put(symbol, next);
								if (next.openTime + 1 >= clock && next.openTime <= clock + MINUTE.ms - 1) {
									clock -= MINUTE.ms;
								}
							}
						}
					}
				} else {
					if (it.hasMoreElements()) {
						Candle next = it.nextElement();
						if (next != null) {
							pending.put(symbol, next);
						}
					}
				}
			}
		}

//		System.out.println("Processed candle counts:" + counts);

		System.out.println("Real time trading simulation from " + start + " to " + end);
		System.out.print(account.toString());
		System.out.println("Approximate holdings: $" + account.getTotalFundsInReferenceCurrency() + " " + account.getReferenceCurrencySymbol());
	}
}
