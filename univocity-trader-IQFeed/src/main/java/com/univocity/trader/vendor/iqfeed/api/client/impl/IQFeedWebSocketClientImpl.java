package com.univocity.trader.vendor.iqfeed.api.client.impl;

import org.asynchttpclient.ws.WebSocketUpgradeHandler;
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


    @Override
    public void close() {}

    // TODO: check on this method here
    private WebSocket createNewWebSocket(String channel, IQFeedApiWebSocketListener<?> listener){
        String streamingURL = String.format("%s/%s", IQFeedApiConstants.WS_API_BASE_URL, channel);
        try {
            return client.prepareGet(streamingUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build());
        } catch(Exception any ) {
            log.error(String.format("Error while creating new websocket connection to %s", streamingURL), any);
        }
        return null;
    }
}
