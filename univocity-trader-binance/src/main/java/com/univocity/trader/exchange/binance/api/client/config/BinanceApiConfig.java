package com.univocity.trader.exchange.binance.api.client.config;

import org.asynchttpclient.AsyncHttpClient;

import static com.univocity.trader.exchange.binance.api.client.constant.BinanceApiConstants.*;

/**
 * Configuration used for Binance operations.
 */
public class BinanceApiConfig {

	/**
	 * API base URL
	 */
	private final String apiBaseUrl;

	/**
	 * WebSocket API base URL
	 */
	private final String wsApiBaseUrl;

    /**
     * WebSocket API base URL
     */
    private final String assetInfoApiBaseUrl;
    
	/**
	 * Network type, SPOT or TEST Network
	 */
	private final boolean isTestNet;

	/**
	 *  Asynchronous Http client
	 */
	private final AsyncHttpClient asyncHttpClient;

	/**
	 * API Key
	 */
	private final String apiKey;

	/**
	 * Secret.
	 */
	private final String secret;

	public BinanceApiConfig() {
		this(null, null, null, false);
	}

    /**
     *
     * @param apiKey
     * @param secret
     * @param isTestNet
     */
	public BinanceApiConfig(final String apiKey, final String secret, final AsyncHttpClient asyncHttpClient, final boolean isTestNet) {
		this.apiKey = apiKey;
		this.secret = secret;
        this.asyncHttpClient = asyncHttpClient;
		this.isTestNet = isTestNet;
		this.apiBaseUrl = isTestNet ? API_TEST_NETWORK_BASE_URL : API_BASE_URL ;
		this.wsApiBaseUrl = isTestNet ? WS_SPOT_TEST_NETWORK_API_BASE_URL : WS_API_BASE_URL ;
		this.assetInfoApiBaseUrl = isTestNet ? ASSET_INFO_API_TEST_NETWORK_BASE_URL : ASSET_INFO_API_BASE_URL;
	}

	/**
	 * Get the Api Key used to authenticate requests to Binance
	 * @return the Api Key
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Get the Secret used to authenticate requests to Binance
	 * @return the Secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * REST API base URL.
	 */
	public String getApiBaseUrl() {
		return apiBaseUrl;
	}

	/**
	 * Streaming API base URL.
	 */
	public String getStreamApiBaseUrl() {
		return wsApiBaseUrl;
	}

	/**
	 * Asset info base URL.
	 */
	public String getAssetInfoApiBaseUrl() {
		return assetInfoApiBaseUrl;
	}

    /**
     * Async HTTP Client
     */
	public AsyncHttpClient getAsyncHttpClient() {
	    return asyncHttpClient;
    }

}
