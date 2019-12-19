package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class AbstractSimulator {

	protected TradingFees tradingFees = SimpleTradingFees.percentage(0.1);
	protected Map<String, SymbolInformation> symbolInformation = new TreeMap<>();
	private LocalDateTime simulationStart;
	private LocalDateTime simulationEnd;
	protected final AccountManager account;

	private Map<String, Double> initialAmounts = new ConcurrentHashMap<>();

	public AbstractSimulator(AccountConfiguration<?> accountConfiguration) {
		SimulatedClientAccount clientAccount = createAccountInstance(accountConfiguration, tradingFees);
		account = clientAccount.getAccount();
		setInitialFunds(1000.0);
	}

	protected SimulatedClientAccount createAccountInstance(AccountConfiguration<?> accountConfiguration, TradingFees tradingFees) {
		return new SimulatedClientAccount(accountConfiguration, tradingFees);
	}

	public void setInitialFunds(double initialFunds) {
		setInitialAmount(account.configuration().referenceCurrency(), initialFunds);
	}

	public double getInitialFunds() {
		return getInitialAmount(account.configuration().referenceCurrency());
	}

	public double getInitialAmount(String symbol) {
		return initialAmounts.getOrDefault(symbol, 0.0);
	}

	public void setInitialAmount(String symbol, double initialAmount) {
		account.setAmount(symbol, initialAmount);
		initialAmounts.put(symbol, initialAmount);
	}

	public final TradingFees getTradingFees() {
		return tradingFees;
	}

	public final void setTradingFees(TradingFees tradingFees) {
		this.tradingFees = tradingFees;
	}

	public final SymbolInformation symbolInformation(String symbol) {
		if (!account.configuration().isSymbolSupported(symbol)) {
			Utils.reportUnknownSymbol(symbol, account.configuration());
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

	protected void resetBalances() {
		account.resetBalances();
		initialAmounts.forEach(account::setAmount);
	}
}
