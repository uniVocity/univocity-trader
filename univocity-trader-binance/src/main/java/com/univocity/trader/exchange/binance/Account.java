package com.univocity.trader.exchange.binance;

import com.univocity.trader.config.*;
import org.apache.commons.lang3.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Account extends AccountConfiguration<Account> {

	private String apiKey;
	private char[] secret;
	private boolean isTestNet;

	public Account(String id) {
		super(id);
	}

	@Override
	protected void readExchangeAccountProperties(String accountId, PropertyBasedConfiguration properties) {
		apiKey = properties.getProperty(accountId + "api.key");

		String s = properties.getProperty(accountId + "api.secret");
		secret = s == null ? null : s.toCharArray();
	}

	public String apiKey() {
		return apiKey;
	}

	public Account apiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public Account secret(String secret) {
		return secret(secret.toCharArray());
	}

	public Account secret(char[] secret) {
		this.secret = secret;
		return this;
	}

	public char[] secret() {
		return secret;
	}

	public boolean isTestNet() {
		return isTestNet;
	}

	public void setTestNet(boolean isTestNet) {
		this.isTestNet = isTestNet;
	}
}