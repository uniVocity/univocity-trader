package com.univocity.trader.exchange.binance.futures.examples.websocket;

import com.univocity.trader.exchange.binance.futures.SubscriptionClient;
import com.univocity.trader.exchange.binance.futures.examples.constants.PrivateConfig;
import com.univocity.trader.exchange.binance.futures.model.enums.CandlestickInterval;

public class SubscribeCandlestick {

    public static void main(String[] args) {

        SubscriptionClient client = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
   
        client.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            System.out.println(event);
            client.unsubscribeAll();
        }), null);

    }

}
