package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.vendor.iqfeed.api.client.domain.market.Candlestick;
import org.asynchttpclient.ws.*;

import java.io.Closeable;
import java.util.List;

public interface IQFeedApiWebSocketClient extends Closeable {

    // todo - add more methods for IQFeed
    WebSocket onCandleStickEvent(String symbol, CandlestickInterval interval, IQFeedApiCallback<CandlestickEvent> callback);

    public List<Candlestick> getCandlestickBars(){

    };

    public List<Candlestick> getCandlestickBars(){

    };
}
