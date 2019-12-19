package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;

import java.time.*;
import java.util.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class AbstractSimulator {

	protected Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	protected final AccountManager account;
	protected final Simulation simulation;

	public AbstractSimulator(AccountConfiguration<?> accountConfiguration, Simulation simulation) {
		this.simulation = simulation.clone();
		SimulatedClientAccount clientAccount = createAccountInstance(accountConfiguration, this.simulation.tradingFees());
		account = clientAccount.getAccount();
	}

	protected SimulatedClientAccount createAccountInstance(AccountConfiguration<?> accountConfiguration, TradingFees tradingFees) {
		return new SimulatedClientAccount(accountConfiguration, tradingFees);
	}

	public final SymbolInformation symbolInformation(String symbol) {
		if (!account.configuration().isSymbolSupported(symbol)) {
			account.configuration().reportUnknownSymbol(symbol);
		}
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
		account.resetBalances();
		simulation.initialAmounts().forEach((symbol, amount) -> {
			if (symbol.equals("")) {
				symbol = account.configuration().referenceCurrency();
			}
			account.setAmount(symbol, amount);
		});
	}
}
