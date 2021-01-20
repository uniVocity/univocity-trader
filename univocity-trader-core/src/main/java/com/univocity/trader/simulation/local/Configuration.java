package com.univocity.trader.simulation.local;

import com.univocity.trader.config.*;

public final class Configuration extends com.univocity.trader.config.Configuration<Configuration, SimulationAccount> {

	Configuration() {
		super("simulator.properties");
	}

	@Override
	protected SimulationAccount newAccountConfiguration(String id) {
		return new SimulationAccount(id);
	}

}