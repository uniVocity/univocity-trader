package com.univocity.trader.exchange.binance.futures.examples.trade;

import com.univocity.trader.exchange.binance.futures.RequestOptions;
import com.univocity.trader.exchange.binance.futures.SyncRequestClient;

import com.univocity.trader.exchange.binance.futures.examples.constants.PrivateConfig;
import com.univocity.trader.exchange.binance.futures.model.enums.*;

public class PostOrder {
    public static void main(String[] args) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
//        System.out.println(syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
//                "1", "1", null, null, null, null));

        // place dual position side order.
        // Switch between dual or both position side, call: com.binance.client.examples.trade.ChangePositionSide
        System.out.println(syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.SHORT, OrderType.LIMIT, TimeInForce.GTC,
                "1", "9000", null, null, null, null, NewOrderRespType.RESULT));
    }
}