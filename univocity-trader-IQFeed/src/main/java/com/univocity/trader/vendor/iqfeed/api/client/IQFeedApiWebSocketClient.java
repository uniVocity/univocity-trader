package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.vendor.iqfeed.api.client.domain.event.CandlestickEvent;
import com.univocity.trader.vendor.iqfeed.api.client.domain.market.Candlestick;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.IQFeedHistoricalRequest;
import com.univocity.trader.vendor.iqfeed.api.client.impl.IQFeedApiWebSocketClientImpl;
import org.asynchttpclient.ws.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;

public interface IQFeedApiWebSocketClient extends Closeable {

    // todo - add more methods for IQFeed

    List<Candlestick> getCandlestickBars(String request);

    List<Candlestick> getHistoricalCandlestickBars(IQFeedHistoricalRequest request);

}
