package com.univocity.trader.exchange.interactivebrokers.model.account;

import java.util.*;

public class AccountValue {
	public final String key;
	public final String value;
	public final String currency;
	public final String name;

	AccountValue(String key, String val, String cur, String accountName) {
		this.key = key;
		value = val;
		currency = cur;
		name = accountName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AccountValue that = (AccountValue) o;
		return Objects.equals(key, that.key) &&
				Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
}