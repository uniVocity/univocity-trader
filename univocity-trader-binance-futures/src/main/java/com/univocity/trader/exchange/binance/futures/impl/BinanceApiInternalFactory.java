package com.univocity.trader.exchange.binance.futures.impl;

import com.univocity.trader.exchange.binance.futures.RequestOptions;
import com.univocity.trader.exchange.binance.futures.SubscriptionClient;
import com.univocity.trader.exchange.binance.futures.SubscriptionOptions;
import com.univocity.trader.exchange.binance.futures.SyncRequestClient;
import org.asynchttpclient.AsyncHttpClient;

import java.net.URI;

public final class BinanceApiInternalFactory {

    private static final BinanceApiInternalFactory instance = new BinanceApiInternalFactory();

    public static BinanceApiInternalFactory getInstance() {
        return instance;
    }

    private BinanceApiInternalFactory() {
    }

    public SyncRequestClient createSyncRequestClient(String apiKey, String secretKey, RequestOptions options) {
        RequestOptions requestOptions = new RequestOptions(options);
        RestApiRequestImpl requestImpl = new RestApiRequestImpl(apiKey, secretKey, requestOptions);
        return new SyncRequestImpl(requestImpl);
    }

    public SubscriptionClient createSubscriptionClient(String apiKey, String secretKey, AsyncHttpClient client) {
        /*
        SubscriptionOptions subscriptionOptions = new SubscriptionOptions(options);
        RequestOptions requestOptions = new RequestOptions();
        try {
            String host = new URI(options.getUri()).getHost();
            requestOptions.setUrl("https://" + host);
        } catch (Exception e) {

        }
        SubscriptionClient webSocketStreamClient = new WebSocketStreamClientImpl(apiKey, secretKey, subscriptionOptions);
         */
        return new WebSocketStreamClientImpl(apiKey, secretKey, client);
    }

}
