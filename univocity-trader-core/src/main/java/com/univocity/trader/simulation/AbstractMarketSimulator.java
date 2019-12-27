package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.strategy.*;
import org.apache.commons.lang3.*;
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
	private final Supplier<Exchange<?, A>> exchangeSupplier;

	protected AbstractMarketSimulator(C configuration, Supplier<Exchange<?, A>> exchangeSupplier) {
		super(configuration);
		this.exchangeSupplier = exchangeSupplier;
	}


	private void initialize() {
		symbolHandlers.clear();
		resetBalances();
	}

	@Override
	protected final void executeSimulation(Collection<Parameters> parameters) {
		for (Parameters p : parameters) {
			initialize();
			executeSimulation(p);
			reportResults();
		}
	}

	protected final void executeSimulation(Parameters parameters) {

		Set<Object> allInstances = new HashSet<>();
		getAllPairs().forEach((symbol, pair) -> {
			String assetSymbol = pair[0];
			String fundSymbol = pair[1];

			List<AccountManager> accountsTradingSymbol = new ArrayList<>();
			for (AccountManager account : accounts()) {
				if (account.configuration().symbolPairs().keySet().contains(symbol)) {
					accountsTradingSymbol.add(account);
				}
			}

			Engine[] engines = new Engine[accountsTradingSymbol.size()];
			for (int i = 0; i < engines.length; i++) {
				AccountManager accountManager = accountsTradingSymbol.get(i);
				SimulatedExchange exchange = new SimulatedExchange(accountManager);
				exchange.setSymbolInformation(this.symbolInformation);
				SymbolPriceDetails symbolPriceDetails = new SymbolPriceDetails(exchange);
//			exchange.setMainTradeSymbols(mainTradeSymbols);

				TradingManager tradingManager = new TradingManager(exchange, symbolPriceDetails, accountManager, assetSymbol, fundSymbol, parameters);

				Engine engine = new Engine(tradingManager, parameters, allInstances);
				engines[i] = engine;
			}

			if (engines.length > 0) {
				symbolHandlers.put(symbol, engines);
			}
		});

		allInstances.clear();

		ConcurrentHashMap<String, Enumeration<Candle>> markets = new ConcurrentHashMap<>();
		HashMap<String, Candle> pending = new HashMap<>();

		LocalDateTime start = getSimulationStart();
		LocalDateTime end = getSimulationEnd();
		final long startTime = getStartTime();
		final long endTime = getEndTime();
		CandleRepository candleRepository = new CandleRepository(configure().database());

		int activeQueries = 0;
		Map<String, CompletableFuture<Enumeration<Candle>>> futures = new HashMap<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (String symbol : symbolHandlers.keySet()) {
			activeQueries++;
			boolean loadAllDataFirst = simulation.cacheCandles() || activeQueries > simulation.activeQueryLimit();

			futures.put(symbol, CompletableFuture.supplyAsync(
					() -> candleRepository.iterate(symbol, start.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC), loadAllDataFirst), executor)
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
		boolean ran = false;

//		Map<String, Long> counts = new TreeMap<>();
		for (long clock = startTime; clock <= endTime; clock += MINUTE.ms) {
			for (Map.Entry<String, Enumeration<Candle>> e : markets.entrySet()) {
				ran = true;
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
		if (!ran) {
			throw new IllegalStateException("No candles processed in real time trading simulation from " + start + " to " + end);
		}

//		System.out.println("Processed candle counts:" + counts);
	}

	private void reportResults() {
		for (AccountManager account : accounts()) {
			String id = account.getClient().getId();
			if (StringUtils.isNotBlank(id)) {
				System.out.println("------------------");
				System.out.println("Client: " + id);
			}
			System.out.print(account.toString());
			System.out.println("Approximate holdings: $" + account.getTotalFundsInReferenceCurrency() + " " + account.getReferenceCurrencySymbol());

		}
	}

	public void backfillHistory() {
		TreeSet<String> allSymbols = new TreeSet<>();
		configuration.accounts().forEach(a -> allSymbols.addAll(a.symbolPairs().keySet()));
		allSymbols.addAll(new CandleRepository(configure().database()).getKnownSymbols());
		backfillHistory(allSymbols);
	}

	public void backfillHistory(String... symbolsToUpdate) {
		LinkedHashSet<String> allSymbols = new LinkedHashSet<>();
		Collections.addAll(allSymbols, symbolsToUpdate);
		backfillHistory(allSymbols);
	}

	public void backfillHistory(Collection<String> symbols) {
		CandleRepository candleRepository = new CandleRepository(configure().database());
		Exchange<?, A> exchange = exchangeSupplier.get();
		final Instant start = simulation.backfillStart();
		for (String symbol : symbols) {
			candleRepository.fillHistoryGaps(exchange, symbol, start, configuration.tickInterval());
		}
	}
}
