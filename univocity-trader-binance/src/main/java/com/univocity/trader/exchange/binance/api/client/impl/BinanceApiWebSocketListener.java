package com.univocity.trader.exchange.binance.api.client.impl;

import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.exception.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import org.asynchttpclient.ws.*;
import org.slf4j.*;

import java.io.*;

public class BinanceApiWebSocketListener<T> implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(BinanceApiWebSocketListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectReader objectReader;
    private final BinanceApiCallback<T> callback;

    private WebSocket webSocket = null;
    private String wsName = null;

    public BinanceApiWebSocketListener(BinanceApiCallback<T> callback, Class<T> eventClass) {
        this.callback = callback;
        this.objectReader = MAPPER.readerFor(eventClass);
    }

    public BinanceApiWebSocketListener(BinanceApiCallback<T> callback, TypeReference reference) {
        this.callback = callback;
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
            T event = objectReader.readValue(payload);
            this.callback.onResponse(event);
        } catch (IOException ex) {
            log.error("Error at WebSocket " + wsName, ex);
            throw new BinanceApiException(ex);
        }
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.webSocket = websocket;
        this.wsName = websocket.toString();
        log.info(String.format("WebSocket %s opened", wsName));
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