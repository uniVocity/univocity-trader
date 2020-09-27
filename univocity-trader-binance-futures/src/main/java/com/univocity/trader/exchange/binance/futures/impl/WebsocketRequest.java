package com.univocity.trader.exchange.binance.futures.impl;

import com.univocity.trader.exchange.binance.futures.SubscriptionErrorHandler;
import com.univocity.trader.exchange.binance.futures.SubscriptionListener;
import com.univocity.trader.exchange.binance.futures.impl.utils.Handler;

class WebsocketRequest<T> {

    WebsocketRequest(SubscriptionListener<T> listener, SubscriptionErrorHandler errorHandler) {
        this.updateCallback = listener;
        this.errorHandler = errorHandler;
    }

    String signatureVersion = "2";
    String name;
    Handler<WebSocketConnection> connectionHandler;
    Handler<WebSocketConnection> authHandler = null;
    final SubscriptionListener<T> updateCallback;
    RestApiJsonParser<T> jsonParser;
    final SubscriptionErrorHandler errorHandler;
}
