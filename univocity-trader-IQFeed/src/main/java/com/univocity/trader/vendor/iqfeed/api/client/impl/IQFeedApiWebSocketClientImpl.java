package com.univocity.trader.vendor.iqfeed.api.client.impl;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiCallback;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiWebSocketClient;
import com.univocity.trader.vendor.iqfeed.api.client.constant.IQFeedApiConstants;
import com.univocity.trader.vendor.iqfeed.api.client.domain.candles.IQFeedCandle;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.IQFeedHistoricalRequest;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.lang3.ObjectUtils;
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
    private IQFeedProcessor processor = null;
    private AsyncHttpClient client = null;
    private WebSocket webSocketClient = null;

    public IQFeedApiWebSocketClientImpl(AsyncHttpClient client, IQFeedApiWebSocketListener<?> listener){
        if(client != null) try {
            this.client = client;
            listener.setProcessor(processor);
            webSocketClient = createNewWebSocket(listener);
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public void sendRequest(IQFeedHistoricalRequest request){
        if(this.webSocketClient.isOpen()){
            try {
                this.processor.setLatestRequest(request);
                this.webSocketClient.sendTextFrame(request.toString());
                log.info("Univocity-IQFeed OUT: " + request);
            } catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }
    public List<IQFeedCandle> getCandlestickBars(IQFeedHistoricalRequest request){
        this.sendRequest(request);
        List<IQFeedCandle> candles = this.processor.getCandles();
        return candles;
    }

    public List<IQFeedCandle> getHistoricalCandlestickBars(IQFeedHistoricalRequest request) {
        this.sendRequest(request);
        List<IQFeedCandle> candles = this.processor.getCandles();
        return candles;
    }

    // TODO: check on this method here
    private WebSocket createNewWebSocket(IQFeedApiWebSocketListener<?> listener){
        listener.setProcessor(processor);
        String streamingUrl = IQFeedApiConstants.HOST + ":" + IQFeedApiConstants.PORT;
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
