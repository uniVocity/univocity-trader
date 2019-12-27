package com.univocity.trader.config;

public class SimulationConfiguration extends Configuration<SimulationConfiguration, SimulationAccount>{

	@Override
	protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
		return new ConfigurationGroup[0];
	}

	@Override
	protected SimulationAccount newAccountConfiguration(String id) {
		return new SimulationAccount(id);
	}
}