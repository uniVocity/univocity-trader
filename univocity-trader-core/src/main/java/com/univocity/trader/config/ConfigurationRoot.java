package com.univocity.trader.config;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public abstract class ConfigurationRoot {

	private final List<ConfigurationGroup> configurationGroups = new ArrayList<>();

	protected abstract void addConfigurationGroup(List<ConfigurationGroup> groups);

	final List<ConfigurationGroup> getConfigurationGroups(){
		return configurationGroups;
	}
}
