package com.univocity.trader.config;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class ConfigurationRoot {

	private final List<ConfigurationGroup> configurationGroups = new ArrayList<>();

	final void loadConfigurationGroups(){
		this.addConfigurationGroups(configurationGroups);
	}

	protected abstract void addConfigurationGroups(List<ConfigurationGroup> groups);

	final List<ConfigurationGroup> getConfigurationGroups(){
		return configurationGroups;
	}
}
