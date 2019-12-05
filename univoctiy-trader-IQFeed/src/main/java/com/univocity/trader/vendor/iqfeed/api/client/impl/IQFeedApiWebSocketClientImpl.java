package com.univocity.trader.vendor.iqfeed.api.client.impl;

import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiCallback;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiWebSocketClient;
import com.univocity.trader.vendor.iqfeed.api.client.domain.event.CandlestickEvent;
import com.univocity.trader.vendor.iqfeed.api.client.domain.market.Candlestick;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.netty.handler.WebSocketHandler;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IQFeedApiWebSocketClientImpl implements IQFeedApiWebSocketClient, Closeable {

    private static final Logger log = LoggerFactory.getLogger(IQFeedApiWebSocketClientImpl.class);

    private final AsyncHttpClient client;

    public IQFeedApiWebSocketClientImpl(AsyncHttpClient client){
        this.client = client;
        EventLoopGroup group = new NioEventLoopGroup();
    }

    @Override
    public List<Candlestick> getCandlestickBars(String symbol, Long start, Long end, TimeInterval tickSize){

    }

    @Override
    public List<Candlestick> getHistoricalCandlestickBars(String symbol, Long start, Long end, TimeInterval tickSize){
        this.client.
    }

    @Override
    public WebSocket onCandleStickEvent(String symbols, CandlestickInterval interval, IQFeedApiCallback<CandlestickEvent> callback){
        final String channel = Arrays.stream(symbols.split(","))
                .map(String :: trim)
                .map(s -> String.format("%s@depth", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new IQFeedApiWebSocketListener<>(callback, CandleStickEvent.class));
    }


    // TODO: check on this method here
    private WebSocket createNewWebSocket(String host, String port, IQFeedApiWebSocketListener<?> listener){

        String streamingUrl = new StringBuilder(host).append(port).toString();
        try {
            return client.prepareGet(streamingUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()).get();
        } catch(Exception any ) {
            log.error(String.format("Error while creating new websocket connection to %s", streamingUrl), any);
        }
        return null;
    }

    if()

    @Override
    public void close() {}

}
