package com.univocity.trader.exchange.binance.api.client.impl;

import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.domain.event.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.fasterxml.jackson.core.type.*;
import org.asynchttpclient.*;
import org.asynchttpclient.ws.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class BinanceApiWebSocketClientImpl implements BinanceApiWebSocketClient, Closeable {

    private static final Logger log = LoggerFactory.getLogger(BinanceApiWebSocketClientImpl.class);

    private final AsyncHttpClient client;

    public BinanceApiWebSocketClientImpl(AsyncHttpClient client) {
        this.client = client;
    }

    @Override
    public WebSocket onDepthEvent(String symbols, BinanceApiCallback<DepthEvent> callback) {
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@depth", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, DepthEvent.class));
    }

    @Override
    public WebSocket onCandlestickEvent(String symbols, CandlestickInterval interval, BinanceApiCallback<CandlestickEvent> callback) {
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@kline_%s", s, interval.getIntervalId()))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, CandlestickEvent.class));
    }

    public WebSocket onAggTradeEvent(String symbols, BinanceApiCallback<AggTradeEvent> callback) {
        final String channel = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@aggTrade", s))
                .collect(Collectors.joining("/"));
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, AggTradeEvent.class));
    }

    public WebSocket onUserDataUpdateEvent(String listenKey, BinanceApiCallback<UserDataUpdateEvent> callback) {
        return createNewWebSocket(listenKey, new BinanceApiWebSocketListener<>(callback, UserDataUpdateEvent.class));
    }

    public WebSocket onAllMarketTickersEvent(BinanceApiCallback<List<AllMarketTickersEvent>> callback) {
        final String channel = "!ticker@arr";
        return createNewWebSocket(channel, new BinanceApiWebSocketListener<>(callback, new TypeReference<List<AllMarketTickersEvent>>() {}));
    }

    /**
     * @deprecated This method is no longer functional. Please use the returned {@link Closeable} from any of the other methods to close the web socket.
     */
    @Override
    public void close() { }

    private WebSocket createNewWebSocket(String channel, BinanceApiWebSocketListener<?> listener) {
        String streamingUrl = String.format("%s/%s", BinanceApiConstants.WS_API_BASE_URL, channel);
        try {
            return client.prepareGet(streamingUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()).get();
        } catch (Exception any) {
            log.error(String.format("Error while creating new websocket connection to %s", streamingUrl), any);
        }
        return null;
    }
}
