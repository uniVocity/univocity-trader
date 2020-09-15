package com.univocity.trader.exchange.binance.futures.examples.trade;

import com.univocity.trader.exchange.binance.futures.RequestOptions;
import com.univocity.trader.exchange.binance.futures.SyncRequestClient;

import com.univocity.trader.exchange.binance.futures.examples.constants.PrivateConfig;

public class GetOrder {
    public static void main(String[] args) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
        System.out.println(syncRequestClient.getOrder("BTCUSDT", 37886301L, null));
    }
}