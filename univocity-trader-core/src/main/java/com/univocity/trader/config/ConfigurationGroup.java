package com.univocity.trader.config;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class ConfigurationGroup<T extends ConfigurationRoot> {

	private final Supplier<T> parent;

	protected ConfigurationGroup(T parent) {
		this(() -> parent);
	}

	protected ConfigurationGroup(Supplier<T> parent) {
		this.parent = parent;
	}

	protected T getParent() {
		return parent.get();
	}

	protected abstract void readProperties(PropertyBasedConfiguration properties);

	public abstract boolean isConfigured();
}
