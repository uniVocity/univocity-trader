package com.univocity.trader.config;

import java.util.*;
import java.util.function.*;

public class AccountList<T extends AccountConfiguration<T>> extends ConfigurationGroup {

	private Map<String, T> accounts = new LinkedHashMap<>();
	private final Supplier<T> accountConfigurationSupplier;

	AccountList(ConfigurationRoot parent, Supplier<T> accountConfigurationSupplier) {
		super(parent);
		this.accountConfigurationSupplier = accountConfigurationSupplier;
	}

	@Override
	protected void readProperties(PropertyBasedConfiguration properties) {
		List<String> accountIds = properties.getOptionalList("accounts");

		if (!accountIds.isEmpty()) {
			for (String accountId : accountIds) {
				if (accounts.containsKey(accountId)) {
					throw new IllegalConfigurationException("Duplicate account ID in properties file: " + accountId);
				}
				account(accountId).readProperties(accountId, properties);
			}
		}
		//always assume an account without ID. We'll get rid of it if not configured
		account().readProperties("", properties);
	}

	@Override
	public boolean isConfigured() {
		return !accounts.isEmpty() && accounts.values().stream().anyMatch(AccountConfiguration::isConfigured);
	}

	public final T account(String accountId) {
		return accounts.computeIfAbsent(accountId, a -> accountConfigurationSupplier.get());
	}

	public final T account() {
		return account("");
	}
}