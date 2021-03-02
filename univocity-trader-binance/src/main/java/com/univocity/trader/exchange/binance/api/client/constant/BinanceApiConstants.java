package com.univocity.trader.exchange.binance.api.client.constant;

import org.apache.commons.lang3.builder.*;

/**
 * Constants used throughout Binance's API.
 */
public class BinanceApiConstants {

	/**
	 * REST API base URL.
	 */
	public static final String API_BASE_URL = "https://api.binance.com";

    /**
     * REST API base URL SPOT Test Network.
     */
    public static final String API_TEST_NETWORK_BASE_URL = "https://testnet.binance.vision";

	/**
	 * Streaming API base URL.
	 */
	public static final String WS_API_BASE_URL = "wss://stream.binance.com:9443/ws";

	/**
	 * Streaming API Test Network base URL.
	 */
	public static final String WS_SPOT_TEST_NETWORK_API_BASE_URL = "wss:/testnet.binance.vision/ws";

	/**
	 * Asset info base URL.
	 */
	public static final String ASSET_INFO_API_BASE_URL = "https://www.binance.com/";

	/**
	 * Asset info Test Network base URL.
	 */
	public static final String ASSET_INFO_API_TEST_NETWORK_BASE_URL = "https://testnet.binance.vision/";

	/**
	 * HTTP Header to be used for API-KEY authentication.
	 */
	public static final String API_KEY_HEADER = "X-MBX-APIKEY";

	/**
	 * Decorator to indicate that an endpoint requires an API key.
	 */
	public static final String ENDPOINT_SECURITY_TYPE_APIKEY = "APIKEY";
	public static final String ENDPOINT_SECURITY_TYPE_APIKEY_HEADER = ENDPOINT_SECURITY_TYPE_APIKEY + ": #";

	/**
	 * Decorator to indicate that an endpoint requires a signature.
	 */
	public static final String ENDPOINT_SECURITY_TYPE_SIGNED = "SIGNED";
	public static final String ENDPOINT_SECURITY_TYPE_SIGNED_HEADER = ENDPOINT_SECURITY_TYPE_SIGNED + ": #";

	/**
	 * Default receiving window.
	 */
	public static final long DEFAULT_RECEIVING_WINDOW = 60_000L;

	/**
	 * Default ToStringStyle used by toString methods.
	 * Override this to change the output format of the overridden toString methods.
	 * - Example ToStringStyle.JSON_STYLE
	 */
	public static ToStringStyle TO_STRING_BUILDER_STYLE = ToStringStyle.SHORT_PREFIX_STYLE;
}
