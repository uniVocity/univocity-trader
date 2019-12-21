package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class AbstractMarketSimulator<C extends Configuration<C, A>, A extends AccountConfiguration<A>> extends AbstractSimulator<C, A> {

	private static final Logger log = LoggerFactory.getLogger(AbstractMarketSimulator.class);

	private final Map<String, Engine[]> symbolHandlers = new HashMap<>();
	private int activeQueryLimit = 15;

	protected AbstractMarketSimulator(C configuration) {
		super(configuration);
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
		symbolHandlers.clear();
		resetBalances();

		Set<Object> allInstances = new HashSet<>();
		getAllPairs().forEach((symbol, pair) -> {
			String assetSymbol = pair[0];
			String fundSymbol = pair[1];

			AccountManager[] accounts = accounts();
			Engine[] engines = new Engine[accounts.length];
			for (int i = 0; i < accounts.length; i++) {
				SimulatedExchange exchange = new SimulatedExchange(accounts[i]);
				exchange.setSymbolInformation(this.symbolInformation);
				SymbolPriceDetails symbolPriceDetails = new SymbolPriceDetails(exchange);
//			exchange.setMainTradeSymbols(mainTradeSymbols);

				TradingManager tradingManager = new TradingManager(exchange, symbolPriceDetails, accounts[i], assetSymbol, fundSymbol, parameters);

				Engine engine = new Engine(tradingManager, parameters, allInstances);
			}

			symbolHandlers.put(symbol, engines);
		});

		allInstances.clear();

		ConcurrentHashMap<String, Enumeration<Candle>> markets = new ConcurrentHashMap<>();
		HashMap<String, Candle> pending = new HashMap<>();

		LocalDateTime start = getSimulationStart();
		LocalDateTime end = getSimulationEnd();
		final long startTime = getStartTime();
		final long endTime = getEndTime();


		int activeQueries = 0;
		Map<String, CompletableFuture<Enumeration<Candle>>> futures = new HashMap<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (String symbol : symbolHandlers.keySet()) {
			activeQueries++;
			boolean loadAllDataFirst = simulation.cacheCandles() || activeQueries > activeQueryLimit;

			futures.put(symbol, CompletableFuture.supplyAsync(
					() -> CandleRepository.iterate(symbol, start.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC), loadAllDataFirst), executor)
			);
		}

		futures.forEach((symbol, candles) -> {
			try {
				markets.put(symbol, candles.get());
			} catch (Exception e) {
				log.error("Error querying " + symbol + " candles from database", e);
			}
		});

		executor.shutdown();

//		Map<String, Long> counts = new TreeMap<>();
		for (long clock = startTime; clock <= endTime; clock += MINUTE.ms) {
			for (Map.Entry<String, Enumeration<Candle>> e : markets.entrySet()) {
				String symbol = e.getKey();
				Enumeration<Candle> it = e.getValue();
				Candle candle = pending.get(symbol);
				if (candle != null) {
					if (candle.openTime + 1 >= clock && candle.openTime <= clock + MINUTE.ms - 1) {
						Engine[] engines = symbolHandlers.get(symbol);

						for (int i = 0; i < engines.length; i++) {
							engines[i].process(candle, false);
						}

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
		for (AccountManager account : accounts()) {
			System.out.print(account.toString());
			System.out.println("Approximate holdings: $" + account.getTotalFundsInReferenceCurrency() + " " + account.getReferenceCurrencySymbol());
		}
	}
}
