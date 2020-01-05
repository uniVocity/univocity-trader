package com.univocity.trader.iqfeed;

import com.univocity.trader.config.*;

/**
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