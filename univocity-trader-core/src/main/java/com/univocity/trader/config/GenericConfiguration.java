package com.univocity.trader.config;

public class GenericConfiguration extends Configuration<GenericConfiguration, Account>{

	@Override
	protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
		return new ConfigurationGroup[0];
	}

	@Override
	protected Account newAccountConfiguration(String id) {
		return new Account(id);
	}
}