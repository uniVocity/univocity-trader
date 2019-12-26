package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;

import java.time.*;
import java.util.*;
import java.util.function.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class AbstractSimulator<C extends Configuration<C, A>, A extends AccountConfiguration<A>> {

	protected Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	private AccountManager[] accounts;
	protected final Simulation simulation;
	private final C configuration;

	private final Supplier<Exchange<?, A>> exchangeSupplier;
	private Map<String, String[]> allPairs;

	public AbstractSimulator(C configuration, Supplier<Exchange<?, A>> exchangeSupplier) {
		this.configuration = configuration;
		simulation = configuration.simulation();
		this.exchangeSupplier = exchangeSupplier;
	}

	protected AccountManager[] accounts() {
		if (accounts == null) {
			List<A> accountConfigs = configuration.accounts();
			if (accountConfigs.isEmpty()) {
				throw new IllegalStateException("No account configuration defined");
			}
			this.accounts = new AccountManager[accountConfigs.size()];
			int i = 0;
			for (A accountConfig : accountConfigs) {
				this.accounts[i++] = createAccountInstance(accountConfig).getAccount();
			}
		}
		return accounts;
	}

	protected SimulatedClientAccount createAccountInstance(A accountConfiguration) {
		return new SimulatedClientAccount(accountConfiguration);
	}

	public final SymbolInformation symbolInformation(String symbol) {
		SymbolInformation info = new SymbolInformation(symbol);
		symbolInformation.put(symbol, info);
		return info;
	}

	public final LocalDateTime getSimulationStart() {
		LocalDateTime start = simulation.simulationStart();
		return start != null ? start : LocalDateTime.now().minusYears(1);
	}

	public final LocalDateTime getSimulationEnd() {
		LocalDateTime end = simulation.simulationEnd();
		return end != null ? end : LocalDateTime.now();
	}

	protected final long getStartTime() {
		return getSimulationStart().toInstant(ZoneOffset.UTC).toEpochMilli() - MINUTE.ms;
	}

	protected final long getEndTime() {
		return getSimulationEnd().toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	protected void resetBalances() {
		for (AccountManager account : accounts()) {
			account.resetBalances();
			double[] total = new double[]{0};
			simulation.initialAmounts().forEach((symbol, amount) -> {
				if (symbol.equals("")) {
					symbol = account.configuration().referenceCurrency();
				}
				account.setAmount(symbol, amount);
				total[0] += amount;
			});

			if (total[0] == 0.0) {
				throw new IllegalStateException("Cannot execute simulation without initial funds to trade with");
			}
		}
	}

//	public A account() {
//		return configuration.account();
//	}
//
//	public A account(String accountId) {
//		return configuration.account(accountId);
//	}

	protected Map<String, String[]> getAllPairs() {
		if (allPairs == null) {
			allPairs = new TreeMap<>();
			for (AccountManager account : accounts()) {
				allPairs.putAll(account.configuration().symbolPairs());
			}
		}
		return allPairs;
	}

	protected Collection<String[]> getTradePairs() {
		return getAllPairs().values();
	}

	public C configure() {
		return configuration;
	}

	public final void run() {
		List<Parameters> parameters = simulation.parameters();
		if (parameters.isEmpty()) {
			parameters = Collections.singletonList(Parameters.NULL);
		}
		for (Parameters params : parameters) {
			executeSimulation(params);
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

	protected abstract void executeSimulation(Parameters parameters);
}
