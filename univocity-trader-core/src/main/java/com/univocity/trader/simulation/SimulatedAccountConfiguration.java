package com.univocity.trader.simulation;

import com.univocity.trader.account.*;

public interface SimulatedAccountConfiguration extends AccountConfiguration {

	SimulatedAccountConfiguration setAmount(String symbol, double amount);

	SimulatedAccountConfiguration lockAmount(String symbol, double amount);

	SimulatedAccountConfiguration resetBalances();
}
