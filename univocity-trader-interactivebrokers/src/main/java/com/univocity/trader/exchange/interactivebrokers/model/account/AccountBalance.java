package com.univocity.trader.exchange.interactivebrokers.model.account;

import com.ib.client.*;

import java.util.*;

public class AccountBalance {
	private Set<PortfolioEntry> entries = new HashSet<>();
	private Set<AccountValue> accountValues = new HashSet<>();

	public void updateAccountValue(String key, String value, String currency, String accountName) {
		accountValues.add(new AccountValue(key, value, currency, accountName));
	}

	public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
		entries.add(new PortfolioEntry(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName));
	}
}




