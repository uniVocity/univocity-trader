package com.univocity.trader.exchange.tdameritrade.impl;

import com.univocity.trader.exchange.tdameritrade.RequestOptions;
import com.univocity.trader.exchange.tdameritrade.SyncRequestClient;

public class TDAmeritradeApiInternalFactory {

    private static final TDAmeritradeApiInternalFactory instance = new TDAmeritradeApiInternalFactory();

//    public SyncRequestClient createSyncRequestClient(String apiKey, String secretKey, RequestOptions options){
//        RequestOptions requestOptions = new RequestOptions(options);
//        RestApiRequestImpl requestImpl = new RestApiRequestImpl(apiKey, secretKey, requestOptions);
//        return new SyncRequestImpl(requestImpl);
//    }
}
