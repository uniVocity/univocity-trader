package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;

import java.util.*;

public abstract class DefaultConfiguration {

	private AccountManager account;

	protected abstract AccountManager createAccount();

	protected AccountManager getAccount() {
		if (account == null) {

		}
		return account;
	}


}

