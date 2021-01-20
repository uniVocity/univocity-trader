package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.base.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class AbstractSimulator<C extends Configuration<C, A>, A extends AccountConfiguration<A>> {

	protected Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	private SimulatedAccountManager[] accounts;
	protected final Simulation simulation;
	protected final C configuration;
	private Map<String, String[]> allPairs;
	private Supplier<SignalRepository> signalRepositorySupplier;
	private SignalRepository signalRepository;

	public AbstractSimulator(C configuration) {
		this.configuration = configuration;
		simulation = configuration.simulation();
	}

	protected SimulatedAccountManager[] accounts() {
		if (accounts == null) {
			List<A> accountConfigs = configuration.accounts();
			if (accountConfigs.isEmpty()) {
				throw new IllegalStateException("No account configuration defined");
			}
			this.accounts = new SimulatedAccountManager[accountConfigs.size()];
			int i = 0;
			for (A accountConfig : accountConfigs) {
				this.accounts[i++] = createAccountInstance(accountConfig).getAccount();
			}
		}
		return accounts;
	}

	protected SimulatedClientAccount createAccountInstance(A accountConfiguration) {
		return new SimulatedClientAccount(accountConfiguration, configuration.simulation(), this::getSignalRepository);
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

	protected final long getStartTime(Period warmUp) {
		LocalDateTime start = getSimulationStart();
		if(warmUp != null){
			start = start.minus(warmUp);
		}

		return start.toInstant(ZoneOffset.UTC).toEpochMilli() - MINUTE.ms;
	}

	protected final long getEndTime() {
		return getSimulationEnd().toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	protected final void resetBalances() {
		for (SimulatedAccountManager account : accounts()) {
			account.resetBalances();
			double[] total = new double[]{0};
			simulation.initialAmounts().forEach((symbol, amount) -> {
				account.setAmount(symbol, amount);
				total[0] += amount;
			});

			if (total[0] == 0.0) {
				throw new IllegalStateException("Cannot execute simulation without initial funds to trade with");
			}
		}
		populateAllPairs();
	}

	protected Map<String, String[]> getAllPairs() {
		if (allPairs == null) {
			allPairs = populateAllPairs();
		}
		return allPairs;
	}

	private Map<String, String[]> populateAllPairs() {
		TreeMap<String, String[]> out = new TreeMap<>();
		for (SimulatedAccountManager account : accounts()) {
			out.putAll(account.getAllSymbolPairs());
		}
		return out;
	}

	private Set<String> populateAllReferenceCurrencies() {
		TreeSet<String> out = new TreeSet<>();
		for (SimulatedAccountManager account : accounts()) {
			out.add(account.getReferenceCurrencySymbol());
		}
		return out;
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

	protected void reportResults(Parameters parameters){
		if(signalRepository != null) {
			signalRepository.save();
		}
	}

	private SignalRepository getSignalRepository() {
		if(signalRepository == null && configuration.signalRepositoryDir() != null){
			signalRepository = new SignalRepository(configuration.signalRepositoryDir());
		}
		return signalRepository;
	}

	protected abstract void executeSimulation(Stream<Parameters> parameters);
}
