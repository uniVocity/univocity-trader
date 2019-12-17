package com.univocity.trader.exchange.binance;

import com.univocity.trader.config.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Binance extends Configuration<Account> {

	private static final Binance instance = new Binance();

	static {
		Configuration.initialize(() -> instance, "binance.properties");
	}

	private Binance() {
	}

	public static Binance getInstance() {
		return (Binance) manager.getInstance();
	}

	public static Binance configure() {
		return (Binance) manager.configure();
	}

	public static Binance load() {
		return (Binance) manager.load();
	}

	public static Binance load(String filePath, String... alternativeFilePaths) {
		return (Binance) manager.load(filePath, alternativeFilePaths);
	}

	public static Binance loadFromCommandLine(String... args) {
		return (Binance) manager.loadFromCommandLine(args);
	}

	@Override
	protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
		return new ConfigurationGroup[0];
	}

	@Override
	protected Account newAccountConfiguration() {
		return new Account();
	}
}
