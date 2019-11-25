package com.univocity.trader.vendor.iqfeed.api.client;

import org.asynchttpclient.ws.*;

import java.io.Closeable;

public interface IQFeedApiWebSocketClient extends Closeable {

    // todo - add more methods for IQFeed
    WebSocket onCandleStickEvent(String symbol, CandleStickInterval interval, IQFeedApiCallback<CandlestickEvent> callback);
}
