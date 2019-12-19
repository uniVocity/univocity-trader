package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.config.*;

public interface SimulatedAccountConfiguration {

	SimulatedAccountConfiguration setAmount(String symbol, double amount);

	SimulatedAccountConfiguration resetBalances();
}
