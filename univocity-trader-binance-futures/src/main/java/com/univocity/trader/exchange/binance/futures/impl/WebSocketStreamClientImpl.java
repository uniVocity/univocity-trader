package com.univocity.trader.exchange.binance.futures.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.univocity.trader.exchange.binance.futures.BinanceFutures;
import com.univocity.trader.exchange.binance.futures.BinanceFuturesApiCallback;
import com.univocity.trader.exchange.binance.futures.SubscriptionClient;
import com.univocity.trader.exchange.binance.futures.SubscriptionErrorHandler;
import com.univocity.trader.exchange.binance.futures.SubscriptionListener;
import com.univocity.trader.exchange.binance.futures.SubscriptionOptions;
import com.univocity.trader.exchange.binance.futures.constant.BinanceApiConstants;
import com.univocity.trader.exchange.binance.futures.impl.utils.Channels;
import com.univocity.trader.exchange.binance.futures.model.enums.CandlestickInterval;
import com.univocity.trader.exchange.binance.futures.model.event.AggregateTradeEvent;
import com.univocity.trader.exchange.binance.futures.model.event.CandlestickEvent;
import com.univocity.trader.exchange.binance.futures.model.event.LiquidationOrderEvent;
import com.univocity.trader.exchange.binance.futures.model.event.MarkPriceEvent;
import com.univocity.trader.exchange.binance.futures.model.event.OrderBookEvent;
import com.univocity.trader.exchange.binance.futures.model.event.SymbolBookTickerEvent;
import com.univocity.trader.exchange.binance.futures.model.event.SymbolMiniTickerEvent;
import com.univocity.trader.exchange.binance.futures.model.event.SymbolTickerEvent;
import com.univocity.trader.exchange.binance.futures.model.user.UserDataUpdateEvent;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WebSocketStreamClientImpl implements SubscriptionClient, Closeable {
    private static final Logger log = LoggerFactory.getLogger(WebSocketStreamClientImpl.class);

    //private final SubscriptionOptions options;
    private WebSocketWatchDog watchDog;

    private final WebsocketRequestImpl requestImpl;

    private final List<WebSocketConnection> connections = new LinkedList<>();

    private final String apiKey;

    private final String secretKey;

    private final AsyncHttpClient client;
/*

    WebSocketStreamClientImpl(String apiKey, String secretKey, SubscriptionOptions options) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.watchDog = null;
        this.options = Objects.requireNonNull(options);

        this.requestImpl = new WebsocketRequestImpl();
    }
*/

    WebSocketStreamClientImpl(String apiKey, String secretKey, AsyncHttpClient client) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        //this.watchDog = null;
        //this.options = Objects.requireNonNull(options);
        this.requestImpl = new WebsocketRequestImpl();

        this.client = client;
    }

    /*
    private <T> void createConnection(WebsocketRequest<T> request, boolean autoClose) {
        if (watchDog == null) {
            watchDog = new WebSocketWatchDog(options);
        }
        WebSocketConnection connection = new WebSocketConnection(apiKey, secretKey, options, request, watchDog,
                autoClose);
        if (autoClose == false) {
            connections.add(connection);
        }
        connection.connect();
    }

    private <T> void createConnection(WebsocketRequest<T> request) {
        createConnection(request, false);
    }
    */

    private WebSocket createNewWebSocket(BinanceFuturesApiWebSocketListener<?> listener) {
        //String streamingUrl = String.format("%s/%s", BinanceApiConstants.WS_API_BASE_URL, channel);
        String streamingUrl = BinanceApiConstants.WS_API_BASE_URL;
        System.out.println(streamingUrl);
        try {
            return client.prepareGet(streamingUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()).get();
        } catch (Exception any) {
            log.error(String.format("Error while creating new websocket connection to %s", streamingUrl), any);
        }
        return null;
    }
/*
    @Override
    public void unsubscribeAll() {
        for (WebSocketConnection connection : connections) {
            watchDog.onClosedNormally(connection);
            connection.close();
        }
        connections.clear();
    }*/

    @Override
    public WebSocket subscribeAggregateTradeEvent(String symbol, BinanceFuturesApiCallback<AggregateTradeEvent> callback) {
        //createConnection(requestImpl.subscribeAggregateTradeEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.aggregateTradeChannel(symbol), AggregateTradeEvent.class));
    }

    @Override
    public WebSocket subscribeMarkPriceEvent(String symbol, BinanceFuturesApiCallback<MarkPriceEvent> callback) {
        //createConnection(requestImpl.subscribeMarkPriceEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.markPriceChannel(symbol), MarkPriceEvent.class));
    }

   /* @Override
    public void subscribeCandlestickEvent(String symbol, CandlestickInterval interval,
            SubscriptionListener<CandlestickEvent> subscriptionListener, 
            SubscriptionErrorHandler errorHandler) {
        createConnection(requestImpl.subscribeCandlestickEvent(symbol, interval, subscriptionListener, errorHandler));
    }
    */
    @Override
    public WebSocket subscribeCandlestickEvent(String symbol, CandlestickInterval interval, BinanceFuturesApiCallback<CandlestickEvent> callback) {
        /*
        final String channel = Arrays.stream(symbol.split(","))
                .map(String::trim)
                .map(s -> String.format("%s@kline_%s", s, interval))
                .collect(Collectors.joining("/"));
        */
        //Channels.markPriceChannel(symbol);
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.candlestickChannel(symbol, interval), CandlestickEvent.class));
    }


    @Override
    public WebSocket subscribeSymbolMiniTickerEvent(String symbol, BinanceFuturesApiCallback<SymbolMiniTickerEvent> callback) {
        //createConnection(requestImpl.subscribeSymbolMiniTickerEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.miniTickerChannel(symbol), SymbolMiniTickerEvent.class));
    }

    @Override
    public WebSocket subscribeAllMiniTickerEvent(BinanceFuturesApiCallback<List<SymbolMiniTickerEvent>> callback) {
        //createConnection(requestImpl.subscribeAllMiniTickerEvent(subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.miniTickerChannel(), new TypeReference<List<SymbolMiniTickerEvent>>(){}));
    }

    @Override
    public WebSocket subscribeSymbolTickerEvent(String symbol, BinanceFuturesApiCallback<SymbolTickerEvent> callback) {
        //createConnection(requestImpl.subscribeSymbolTickerEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.tickerChannel(symbol), SymbolTickerEvent.class));
    }

    @Override
    public WebSocket subscribeAllTickerEvent(BinanceFuturesApiCallback<List<SymbolTickerEvent>> callback) {
       // createConnection(requestImpl.subscribeAllTickerEvent(subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.tickerChannel(), new TypeReference<List<SymbolTickerEvent>>(){}));
    }

    @Override
    public WebSocket subscribeSymbolBookTickerEvent(String symbol, BinanceFuturesApiCallback<SymbolBookTickerEvent> callback) {
        //createConnection(requestImpl.subscribeSymbolBookTickerEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.bookTickerChannel(symbol), SymbolBookTickerEvent.class));
    }

    @Override
    public WebSocket subscribeAllBookTickerEvent(BinanceFuturesApiCallback<SymbolBookTickerEvent> callback) {
        //createConnection(requestImpl.subscribeAllBookTickerEvent(subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.bookTickerChannel(), SymbolBookTickerEvent.class));
    }

    @Override
    public WebSocket subscribeSymbolLiquidationOrderEvent(String symbol, BinanceFuturesApiCallback<LiquidationOrderEvent> callback) {
        //createConnection(requestImpl.subscribeSymbolLiquidationOrderEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.liquidationOrderChannel(symbol), LiquidationOrderEvent.class));
    }

    @Override
    public WebSocket subscribeAllLiquidationOrderEvent(BinanceFuturesApiCallback<LiquidationOrderEvent> callback) {
        //createConnection(requestImpl.subscribeAllLiquidationOrderEvent(subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.liquidationOrderChannel(), LiquidationOrderEvent.class));
    }

    @Override
    public WebSocket subscribeBookDepthEvent(String symbol, Integer limit, BinanceFuturesApiCallback<OrderBookEvent> callback) {
        //createConnection(requestImpl.subscribeBookDepthEvent(symbol, limit, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.bookDepthChannel(symbol, limit), OrderBookEvent.class));
    }

    @Override
    public WebSocket subscribeDiffDepthEvent(String symbol, BinanceFuturesApiCallback<OrderBookEvent> callback) {
        //createConnection(requestImpl.subscribeDiffDepthEvent(symbol, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.diffDepthChannel(symbol), OrderBookEvent.class));
    }

    @Override
    public WebSocket subscribeUserDataEvent(String listenKey, BinanceFuturesApiCallback<UserDataUpdateEvent> callback) {
        //createConnection(requestImpl.subscribeUserDataEvent(listenKey, subscriptionListener, errorHandler));
        return createNewWebSocket(new BinanceFuturesApiWebSocketListener<>(callback, Channels.userDataChannel(listenKey), UserDataUpdateEvent.class));
    }


    @Override
    public void close() throws IOException {

    }
}
