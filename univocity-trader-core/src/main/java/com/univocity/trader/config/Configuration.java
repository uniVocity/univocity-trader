package com.univocity.trader.config;

import com.univocity.trader.indicators.base.*;

import java.util.*;

public abstract class Configuration<C extends Configuration<C, T>, T extends AccountConfiguration<T>> {

	private final List<ConfigurationGroup> configurationGroups = new ArrayList<>();
	private final ConfigurationManager<C> manager;

	private final DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
	private final EmailConfiguration emailConfiguration = new EmailConfiguration();
	private final Simulation simulation = new Simulation();
	final AccountList<T> accountList = new AccountList<T>(this::newAccountConfiguration);
	private TimeInterval tickInterval;

	protected Configuration() {
		this("univocity-trader.properties");
	}

	final void loadConfigurationGroups(){
		this.addConfigurationGroups(configurationGroups);
	}

	final List<ConfigurationGroup> getConfigurationGroups(){
		return configurationGroups;
	}

	protected Configuration(String defaultConfigurationFile) {
		manager = new ConfigurationManager<C>((C)this, defaultConfigurationFile);
	}

	public C configure() {
		return manager.configure();
	}

	public C loadConfigurationFromProperties() {
		return manager.load();
	}

	public C loadConfigurationFromProperties(String filePath, String... alternativeFilePaths) {
		return manager.load(filePath, alternativeFilePaths);
	}

	public C loadConfigurationFromCommandLine(String... args) {
		return manager.loadFromCommandLine(args);
	}

	protected final void addConfigurationGroups(List<ConfigurationGroup> groups) {
		groups.add(databaseConfiguration);
		groups.add(emailConfiguration);
		groups.add(accountList);
		groups.add(simulation);

		ConfigurationGroup[] additionalGroups = getAdditionalConfigurationGroups();
		if (additionalGroups != null) {
			Collections.addAll(groups, additionalGroups);
		}
	}

	protected abstract ConfigurationGroup[] getAdditionalConfigurationGroups();

	public DatabaseConfiguration database() {
		return databaseConfiguration;
	}

	public EmailConfiguration mailSender() {
		return emailConfiguration;
	}

	public Simulation simulation() {
		return simulation;
	}

	public T account() {
		return accountList.account();
	}

	public T account(String accountId) {
		return accountList.account(accountId);
	}

	public List<T> accounts(){
		return accountList.accounts();
	}

	public TimeInterval tickInterval() {
		return tickInterval;
	}

	public C tickInterval(TimeInterval tickInterval) {
		 this.tickInterval = tickInterval;
		 return (C)this;
	}

	protected abstract T newAccountConfiguration(String id);
}