package com.univocity.trader.config;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class AccountList<T extends AccountConfiguration<T>> implements ConfigurationGroup {

	private Map<String, T> accounts = new LinkedHashMap<>();
	private final Function<String, T> accountConfigurationSupplier;

	AccountList(Function<String, T> accountConfigurationSupplier) {
		this.accountConfigurationSupplier = accountConfigurationSupplier;
	}

	public void readProperties(PropertyBasedConfiguration properties) {
		List<String> accountIds = properties.getOptionalList("accounts");

		if (!accountIds.isEmpty()) {
			for (String accountId : accountIds) {
				if (accounts.containsKey(accountId)) {
					throw new IllegalConfigurationException("Duplicate account ID in properties file: " + accountId);
				}
				account(accountId).readProperties(accountId, properties);
			}
		} else {
			//look for an account without ID.
			account().readProperties("", properties);
		}
	}

	@Override
	public boolean isConfigured() {
		return !accounts.isEmpty() && accounts.values().stream().anyMatch(AccountConfiguration::isConfigured);
	}

	public final T account(String accountId) {
		return accounts.computeIfAbsent(accountId, a -> accountConfigurationSupplier.apply(accountId));
	}

	public final T account() {
		if (accounts.size() == 1) {
			return accounts.values().iterator().next();
		} else if (accounts.size() > 1) {
			throw new IllegalArgumentException("Please provide an account ID when multiple accounts are in use. Available accounts: " + accounts.keySet());
		}
		return account("");
	}

	public List<T> accounts() {
		return accounts.values().stream().filter(AccountConfiguration::isConfigured).collect(Collectors.toList());
	}
}