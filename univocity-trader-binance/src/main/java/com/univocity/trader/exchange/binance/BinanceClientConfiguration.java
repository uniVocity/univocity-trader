package com.univocity.trader.exchange.binance;

import com.univocity.trader.config.*;
import org.apache.commons.lang3.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class BinanceClientConfiguration extends ClientConfiguration<BinanceClientConfiguration> {

	private String apiKey;
	private char[] secret;

	public BinanceClientConfiguration() {
		super();
	}

	@Override
	protected void readClientProperties(String clientId, PropertyBasedConfiguration properties) {
		apiKey = properties.getProperty(clientId + "api.key");

		String s = properties.getProperty(clientId + "api.secret");
		secret = s == null ? null : s.toCharArray();
	}

	public String apiKey() {
		return apiKey;
	}

	public BinanceClientConfiguration apiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public BinanceClientConfiguration secret(String secret) {
		return secret(secret.toCharArray());
	}

	public BinanceClientConfiguration secret(char[] secret) {
		this.secret = secret;
		return this;
	}

	public char[] secret() {
		return secret;
	}

	@Override
	public boolean isConfigured() {
		return super.isConfigured() && StringUtils.isNoneBlank(apiKey) && ArrayUtils.isNotEmpty(secret);
	}
}