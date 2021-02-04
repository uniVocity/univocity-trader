package com.univocity.trader.exchange.binance.futures.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.univocity.trader.exchange.binance.futures.BinanceFuturesApiCallback;
import com.univocity.trader.exchange.binance.futures.exception.BinanceApiException;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BinanceFuturesApiWebSocketListener<T> implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(BinanceFuturesApiWebSocketListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectReader objectReader;
    private final BinanceFuturesApiCallback<T> callback;

    private WebSocket webSocket = null;
    private String wsName = null;
    private String channel = null;
    private long lastTime = 0;

    public BinanceFuturesApiWebSocketListener(BinanceFuturesApiCallback<T> callback, String channel, Class<T> eventClass) {
        this.callback = callback;
        this.channel = channel;
        this.objectReader = MAPPER.readerFor(eventClass);
    }

    public BinanceFuturesApiWebSocketListener(BinanceFuturesApiCallback<T> callback, String channel, TypeReference reference) {
        this.callback = callback;
        this.channel = channel;
        this.objectReader = MAPPER.readerFor(reference);
    }

    @Override
    public void onPingFrame(byte[] payload) {
        log.info(String.format("WebSocket %s received ping, sending pong back..", wsName));
        this.webSocket.sendPongFrame(payload);
    }

    /**
     * Remember that callback should never block event loop!!
     */
    @Override
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
        try {
            if(lastTime > 0) {
                T event = objectReader.readValue(payload);
                this.callback.onResponse(event);
            }
            lastTime = System.currentTimeMillis();
        } catch (IOException ex) {
            log.error("Error at WebSocket " + wsName, ex);
            throw new BinanceApiException(BinanceApiException.KEY_MISSING, ex.getMessage(), ex);
        }
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.webSocket = websocket;
        this.wsName = websocket.toString();
        log.info(String.format("WebSocket %s opened", wsName));
        if(StringUtils.isNotBlank(channel)) {
            log.info(String.format("WebSocket %s received ping, sending %s", wsName, channel));
            this.webSocket.sendTextFrame(channel);
        }
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        log.warn("WebSocket {} was closed... Code {}, Reason {}", wsName,code, reason);
        callback.onClose();
    }

    @Override
    public void onError(Throwable t) {
        log.error(String.format("Error at WebSocket %s: ", wsName), t);
    }

    public WebSocket getWebSocket() {
        return this.webSocket;
    }
}