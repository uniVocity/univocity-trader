package com.univocity.trader.exchange.interactivebrokers;

import com.univocity.trader.config.*;

/**
 * InteractiveBrokers doesn't require an account login. It simply connects to their Trader Workstation
 * which must be running locally.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Account extends AccountConfiguration<Account> {

	public Account(String id) {
		super(id);
	}

	@Override
	protected void readExchangeAccountProperties(String accountId, PropertyBasedConfiguration properties) {

	}
}