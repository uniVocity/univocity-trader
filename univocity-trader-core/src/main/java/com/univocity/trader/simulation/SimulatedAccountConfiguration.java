package com.univocity.trader.simulation;

public interface SimulatedAccountConfiguration {

	SimulatedAccountConfiguration setAmount(String symbol, double amount);

	SimulatedAccountConfiguration resetBalances();
}
