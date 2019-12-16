package com.univocity.trader.config;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
abstract class ConfigurationGroup {

	private final Supplier<UnivocityConfiguration> parent;

	ConfigurationGroup(UnivocityConfiguration parent) {
		this(() -> parent);
	}

	ConfigurationGroup(Supplier<UnivocityConfiguration> parent) {
		this.parent = parent;
	}

	public DatabaseConfiguration database() {
		return parent.get().databaseConfiguration;
	}

	public EmailConfiguration email() {
		return parent.get().emailConfiguration;
	}

	abstract void readProperties(PropertyBasedConfiguration properties);

	abstract public boolean isConfigured();
}
