package com.univocity.trader.vendor.iqfeed.api.client.impl;

import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiWebSocketClient;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IQFeedAPIWebSocketClientImpl implements IQFeedApiWebSocketClient, Closeable {

    private static final Logger log = LoggerFactory.getLogger(IQFeedAPIWebSocketClientImpl.class);

    private final AsyncHttpClient client;

    public IQFeedApiWebSocketClientImpl(AsyncHttpClient client){
        this.client = client;
    }

    @Override
    public WebSocket onDepthEvent(String symbols, IQFeedApiCallback<DepthEvent> callback){
        final String channel = Arrays.stream
    }

    @Override
    public WebSocket onCandleStickEvent(String symbols, CandlestickInterval interval, IQFeedApiCallback<CandlestickEvent> callback){
        final String channel = Arrays.stream(symbols.split(","))
                .map(String :: trim)
                .map(s -> String.format("%s@depth", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new IQFeedAPIWebSocketListener<>(callback, DepthEvent.class));
    }


    // TODO: check on this method here
    private WebSocket createNewWebSocket(String host, String port, IQFeedApiWebSocketListener<?> listener){

        String streamingUrl = new StringBuilder(host).append(port).toString();
        try {
            return client.prepareGet(streamingUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build());
        } catch(Exception any ) {
            log.error(String.format("Error while creating new websocket connection to %s", streamingUrl), any);
        }
        return null;
    }

    @Override
    public void close() {}

}
