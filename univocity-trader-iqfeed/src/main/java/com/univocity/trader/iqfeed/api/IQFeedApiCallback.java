package com.univocity.trader.iqfeed.api;

@FunctionalInterface
public interface IQFeedApiCallback<T> {

	void onResponse(T response);

	default void onFailure(Throwable cause) {
	}

	default void onClose() {

	}
}
