package com.univocity.trader.exchange.tdameritrade;

import com.univocity.trader.exchange.tdameritrade.model.Auth.EASObject;

/**
 * Synchronous request interface to invoke TDA API
 */
public interface SyncRequestClient {

    static SyncRequestClient create() { return create();}

    static SyncRequestClient create(){
        return TDAmeritradeApiInternalFactory.getInstance().createSyncRequestClient();
    }

//    CandleList getPriceHistory(PeriodType periodType, Frequency frequency, Long startTime, Long endTime, boolean extendedHoursData);
    EASObject postAccessToken();
}
