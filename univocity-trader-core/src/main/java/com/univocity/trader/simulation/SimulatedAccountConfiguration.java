package com.univocity.trader.simulation;

import com.univocity.trader.account.*;

public interface SimulatedAccountConfiguration extends AccountConfiguration {

	SimulatedAccountConfiguration setAmount(String symbol, double cash);

	SimulatedAccountConfiguration resetBalances();
}
