package com.univocity.trader.vendor.iqfeed.api.client.impl;

import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiCallback;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiWebSocketClient;
import com.univocity.trader.vendor.iqfeed.api.client.domain.event.CandlestickEvent;
import com.univocity.trader.vendor.iqfeed.api.client.domain.market.Candlestick;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.IQFeedHistoricalRequest;
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
    private final IQFeedProcessor processor;
    private final AsyncHttpClient client;
    private final WebSocket webSocketClient;

    public IQFeedApiWebSocketClientImpl(AsyncHttpClient client, String host, String port, IQFeedApiWebSocketListener<?> listener){
        try {
            processor = new IQFeedProcessor();
            this.client = client;
            listener.setProcessor(processor);
            webSocketClient = createNewWebSocket(host, port, listener);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public void sendRequest(String request){
        if(this.webSocketClient.isOpen()){
            try {
                this.webSocketClient.sendTextFrame(request);
                log.info("Univocity-IQFeed OUT: " + request);
            } catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }

    public List<Candlestick> getCandlestickBars(IQFeedHistoricalRequest request){
        this.processor.setLatestHeader(request.getHeader());
        this.sendRequest(request.toString());
        List<Candlestick> candles = this.processor.getCandles();
        return candles;
    }

    public List<Candlestick> getCandlestickBars(String request){
        this.sendRequest(request);
        List<Candlestick> candles = this.processor.getCandles();
        return candles;
    }

    public List<Candlestick> getHistoricalCandlestickBars(IQFeedHistoricalRequest request) {
        this.sendRequest(request.toString());
        List<Candlestick> candles = this.processor.getCandles();
        return candles;
    }


    // TODO: check on this method here
    private WebSocket createNewWebSocket(String host, String port, IQFeedApiWebSocketListener<?> listener){
        listener.setProcessor(processor);
        String streamingUrl = new StringBuilder(host).append(port).toString();
        try {
            return client.prepareGet(streamingUrl).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()).get();
        } catch(Exception any ) {
            log.error(String.format("Error while creating new websocket connection to %s", streamingUrl), any);
        }
        return null;
    }


    @Override
    public void close() {}

}
