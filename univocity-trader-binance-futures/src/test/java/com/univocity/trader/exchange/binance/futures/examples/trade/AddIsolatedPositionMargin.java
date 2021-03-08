package com.univocity.trader.exchange.binance.futures.examples.trade;

import com.univocity.trader.exchange.binance.futures.RequestOptions;
import com.univocity.trader.exchange.binance.futures.SyncRequestClient;
import com.univocity.trader.exchange.binance.futures.examples.constants.PrivateConfig;
import com.univocity.trader.exchange.binance.futures.model.enums.PositionSide;

/**
 * @author : wangwanlu
 * @since : 2020/4/23, Thu
 **/
public class AddIsolatedPositionMargin {

    static int INCREASE_MARGIN_TYPE = 1;
    static int DECREASE_MARGIN_TYPE = 2;

    public static void main(String[] args) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);

        System.out.println(syncRequestClient.addIsolatedPositionMargin("BTCUSDT", INCREASE_MARGIN_TYPE, "100", PositionSide.BOTH));
    }
}
