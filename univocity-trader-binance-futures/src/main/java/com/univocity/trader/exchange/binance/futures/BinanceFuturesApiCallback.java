package com.univocity.trader.exchange.binance.futures;

/**
 * BinanceApiCallback is a functional interface used together with the BinanceApiAsyncClient to provide a non-blocking REST client.
 *
 * @param <T> the return type from the callback
 */
@FunctionalInterface
public interface BinanceFuturesApiCallback<T> {

	/**
	 * Called whenever a response comes back from the Binance API.
	 *
	 * @param response the expected response object
	 */
	void onResponse(T response);

	/**
	 * Called whenever an error occurs.
	 *
	 * @param cause the cause of the failure
	 */
	default void onFailure(Throwable cause) {
	}

	default void onClose(){

	}
}