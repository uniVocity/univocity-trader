package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.time.*;
import java.util.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class AbstractSimulator extends DefaultConfiguration {

	protected TradingFees tradingFees = SimpleTradingFees.percentage(0.1);
	protected Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	protected double initialFunds = 1000.0;
	private LocalDateTime simulationStart;
	private LocalDateTime simulationEnd;

	public AbstractSimulator(String referenceCurrency) {
		super(referenceCurrency);
	}

	public final SimulatedAccountConfiguration account() {
		return getAccount();
	}

	@Override
	protected final AccountManager createAccount() {
		SimulatedClientAccount accountApi = new SimulatedClientAccount(getReferenceCurrency(), tradingFees);
		AccountManager account = accountApi.getAccount();
		account.setAmount(getReferenceCurrency(), initialFunds);
		return account;
	}

	public double getInitialFunds() {
		return initialFunds;
	}

	public void setInitialFunds(double initialFunds) {
		this.initialFunds = initialFunds;
	}

	public final TradingFees getTradingFees() {
		return tradingFees;
	}

	public final void setTradingFees(TradingFees tradingFees) {
		this.tradingFees = tradingFees;
	}

	public final SymbolInformation symbolInformation(String symbol) {
		if (!symbolPairs.containsKey(symbol)) {
			throw new IllegalArgumentException("Unknown symbol '" + symbol + "'. Available trading symbols: " + symbolPairs.keySet());
		}
		SymbolInformation info = new SymbolInformation(symbol);
		symbolInformation.put(symbol, info);
		return info;
	}

	public final LocalDateTime getSimulationStart() {
		return simulationStart != null ? simulationStart : LocalDateTime.now().minusYears(1);
	}

	public final void setSimulationStart(LocalDateTime simulationStart) {
		this.simulationStart = simulationStart;
	}

	public final LocalDateTime getSimulationEnd() {
		return simulationEnd != null ? simulationEnd : LocalDateTime.now();
	}

	public final void setSimulationEnd(LocalDateTime simulationEnd) {
		this.simulationEnd = simulationEnd;
	}

	protected final long getStartTime() {
		return getSimulationStart().toInstant(ZoneOffset.UTC).toEpochMilli() - MINUTE.ms;
	}

	protected final long getEndTime() {
		return getSimulationEnd().toInstant(ZoneOffset.UTC).toEpochMilli();
	}
}
