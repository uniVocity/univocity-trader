package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.vendor.iqfeed.api.client.domain.candles.IQFeedCandle;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.IQFeedHistoricalRequest;
import com.univocity.trader.vendor.iqfeed.api.client.impl.IQFeedApiWebSocketClientImpl;
import org.asynchttpclient.ws.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;

public interface IQFeedApiWebSocketClient extends Closeable {

    // todo - add more methods for IQFeed

    List<IQFeedCandle> getCandlestickBars(IQFeedHistoricalRequest request);

    List<IQFeedCandle> getHistoricalCandlestickBars(IQFeedHistoricalRequest request);

}
