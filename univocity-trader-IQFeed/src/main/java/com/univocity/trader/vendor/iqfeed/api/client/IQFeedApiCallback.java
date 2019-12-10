package com.univocity.trader.vendor.iqfeed.api.client;

@FunctionalInterface
public interface IQFeedApiCallback<T> {

    void onResponse(T response);

    default void onFailure(Throwable cause) {
    }

    default void onClose(){

    }
}
