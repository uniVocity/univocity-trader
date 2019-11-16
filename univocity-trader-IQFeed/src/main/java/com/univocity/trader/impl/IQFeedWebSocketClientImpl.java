package com.univocity.trader.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.http.WebSocket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IQFeedWebSocketClientImpl implements IQFeedAPIWebSocketClient, Closeable {
    private static final Logger log = LoggerFactory.getLogger(IQFeedWebSocketClientImpl.class);

    @Override
    public WebSocket onCandleStickEvent(String symbols, CandlestickInterval interval, IQFeedApiCallback<CandleStickEvent> callback){
        final String channel = Arrays.stream(symbols.split(","))
                .map(String :: trim)
                .map(s -> String.format("%s@depth", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new IQFeedAPIWebSocketListener<>(callback, DepthEvent.class));
    }
}
