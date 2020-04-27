package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class AbstractSimulator<C extends Configuration<C, A>, A extends AccountConfiguration<A>> {

	protected Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	private AccountManager[] accounts;
	protected final Simulation simulation;
	protected final C configuration;
	private Map<String, String[]> allPairs;

	public AbstractSimulator(C configuration) {
		this.configuration = configuration;
		simulation = configuration.simulation();
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
		return new SimulatedClientAccount(accountConfiguration, configuration.simulation());
	}

	public final SymbolInformation symbolInformation(String symbol) {
		SymbolInformation info = new SymbolInformation(symbol);
		var allPairs = populateAllPairs();
		var allReferenceCurrencies = populateAllReferenceCurrencies();
		if (!allPairs.containsKey(symbol) && !allReferenceCurrencies.contains(symbol)) {
			throw new IllegalArgumentException("Unknown symbol '" + symbol + "'. Available symbols are: " + allPairs.keySet() + " and reference currencies: " + allReferenceCurrencies);
		}

		symbolInformation.put(symbol, info);
		return info;
	}

	public final LocalDateTime getSimulationStart() {
		LocalDateTime start = simulation.simulateFrom();
		return start != null ? start : LocalDateTime.now().minusYears(1);
	}

	public final LocalDateTime getSimulationEnd() {
		LocalDateTime end = simulation.simulateTo();
		return end != null ? end : LocalDateTime.now();
	}

	protected final long getStartTime() {
		return getSimulationStart().toInstant(ZoneOffset.UTC).toEpochMilli() - MINUTE.ms;
	}

	protected final long getEndTime() {
		return getSimulationEnd().toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	protected final void resetBalances() {
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

	public final Map<String, String[]> allPairs() {
		return populateAllPairs();
	}

	protected Map<String, String[]> getAllPairs() {
		if (allPairs == null) {
			allPairs = populateAllPairs();
		}
		return allPairs;
	}

	private Map<String, String[]> populateAllPairs() {
		TreeMap<String, String[]> out = new TreeMap<>();
		for (AccountManager account : accounts()) {
			out.putAll(account.configuration().symbolPairs());
		}
		return out;
	}

	private Set<String> populateAllReferenceCurrencies() {
		TreeSet<String> out = new TreeSet<>();
		for (AccountManager account : accounts()) {
			out.add(account.getReferenceCurrencySymbol());
		}
		return out;
	}

	protected final Collection<String[]> getTradePairs() {
		return getAllPairs().values();
	}

	public final C configure() {
		return configuration;
	}

	public final void run() {
		Stream<Parameters> parameters = simulation.parameters();
		if (parameters == null) {
			parameters = Stream.of(Parameters.NULL);
		} else {
			System.out.println("Running simulation with parameters - " + new Date());
		}

		long start = System.currentTimeMillis();
		try {
			executeSimulation(parameters);
		} finally {
			System.out.println("Total simulation time: " + TimeInterval.getFormattedDuration(System.currentTimeMillis() - start));
		}
	}

	protected abstract void executeSimulation(Stream<Parameters> parameters);
}
