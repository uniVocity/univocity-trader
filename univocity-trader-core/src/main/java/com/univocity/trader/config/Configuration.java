package com.univocity.trader.config;

import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public abstract class Configuration<C extends Configuration<C, T>, T extends AccountConfiguration<T>> implements ConfigurationGroup {

	private final List<ConfigurationGroup> configurationGroups = new ArrayList<>();
	private final ConfigurationManager<C> manager;

	private final DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
	private final FileRepositoryConfiguration fileRepositoryConfiguration = new FileRepositoryConfiguration();
	private final EmailConfiguration emailConfiguration = new EmailConfiguration();
	private final Simulation simulation = new Simulation();
	final AccountList<T> accountList = new AccountList<T>(this::newAccountConfiguration);
	private TimeInterval tickInterval = minutes(1);
	private boolean updateHistoryBeforeLiveTrading = true;
	private boolean pollCandles = true;
	private Period warmUpPeriod;
	private final RepositoryDir signalRepositoryDir = new RepositoryDir();
	private boolean isTestNet;

	protected Configuration() {
		this("univocity-trader.properties");
	}

	final void loadConfigurationGroups() {
		this.addConfigurationGroups(configurationGroups);
	}

	final List<ConfigurationGroup> getConfigurationGroups() {
		return configurationGroups;
	}

	protected Configuration(String defaultConfigurationFile) {
		manager = new ConfigurationManager<>((C) this, defaultConfigurationFile);
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

	protected final void addConfigurationGroups(List<ConfigurationGroup> groups) {
		groups.add(databaseConfiguration);
		groups.add(emailConfiguration);
		groups.add(accountList);
		groups.add(simulation);
		groups.add(this);

		ConfigurationGroup[] additionalGroups = getAdditionalConfigurationGroups();
		if (additionalGroups != null) {
			Collections.addAll(groups, additionalGroups);
		}
	}

	protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
		return new ConfigurationGroup[0];
	}

	public DatabaseConfiguration database() {
		return databaseConfiguration;
	}

	public FileRepositoryConfiguration fileRepository() {
		return fileRepositoryConfiguration;
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

	public List<T> accounts() {
		return accountList.accounts();
	}

	public TimeInterval tickInterval() {
		return tickInterval;
	}

	public C tickInterval(TimeInterval tickInterval) {
		this.tickInterval = tickInterval;
		return (C) this;
	}

	@Override
	public boolean isConfigured() {
		return tickInterval != null;
	}

	@Override
	public final void readProperties(PropertyBasedConfiguration properties) {
		this.tickInterval = TimeInterval.fromString(properties.getProperty("tick.interval"));
	}

	protected abstract T newAccountConfiguration(String id);

	public boolean updateHistoryBeforeLiveTrading() {
		return updateHistoryBeforeLiveTrading;
	}

	public C updateHistoryBeforeLiveTrading(boolean updateHistoryBeforeLiveTrading) {
		this.updateHistoryBeforeLiveTrading = updateHistoryBeforeLiveTrading;
		return (C) this;
	}

	public boolean pollCandles() {
		return pollCandles;
	}

	public C pollCandles(boolean pollCandles) {
		this.pollCandles = pollCandles;
		return (C) this;
	}


	public Period warmUpPeriod() {
		return warmUpPeriod == null ? Period.ZERO : warmUpPeriod;
	}

	public C warmUpPeriod(Period warmUpPeriod) {
		this.warmUpPeriod = warmUpPeriod;
		return (C) this;
	}

	public RepositoryDir signalRepositoryDir() {
		return signalRepositoryDir;
	}

	public C signalRepositoryDir(File signalRepositoryDir) {
		this.signalRepositoryDir.set(signalRepositoryDir);
		return (C) this;
	}

	public C signalRepositoryDir(Path signalRepositoryDir) {
		this.signalRepositoryDir.set(signalRepositoryDir);
		return (C) this;
	}

	public boolean isTestNet() {
		return isTestNet;
	}

	public void setTestNet(boolean testNet) {
		isTestNet = testNet;
	}
}