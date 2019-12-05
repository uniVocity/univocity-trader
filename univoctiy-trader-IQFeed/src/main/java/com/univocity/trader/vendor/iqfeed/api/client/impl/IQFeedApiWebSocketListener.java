package com.univocity.trader.vendor.iqfeed.api.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiCallback;
import com.univocity.trader.vendor.iqfeed.api.client.exception.IQFeedApiException;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class IQFeedApiWebSocketListener<T> implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(IQFeedApiWebSocketListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectReader objectReader;
    private final IQFeedApiCallback<T> callback;

    private WebSocket webSocket = null;
    private String wsName = null;
    private IQFeedProcessor processor;

    public IQFeedApiWebSocketListener(){
        this.processor = new IQFeedProcessor();
    }

    public IQFeedApiWebSocketListener(Class<T> eventClass) {
        this.processor = new IQFeedProcessor();
        this.objectReader = MAPPER.readerFor(eventClass);
    }

    public IQFeedApiWebSocketListener(IQFeedApiCallback<T> callback, TypeReference reference) {
        this.processor = new IQFeedProcessor();
        this.callback = callback;
        this.objectReader = MAPPER.readerFor(reference);
    }

    @Override
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
        try {
            this.processor.process(payload);
        } catch (Exception ex) {
            log.error("Error at WebSocket " + wsName, ex);
            throw new IQFeedApiException(ex);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket){
        this.webSocket = webSocket;
        this.wsName = webSocket.toString();
        log.info(String.format("WebSocket %s opened", wsName));
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        log.warn("WebSocket {} was closed ... Code {}, Reason {}", wsName, code, reason);
        callback.onClose();
    }

    @Override
    public void onError(Throwable t) {log.error(String.format("Error at WebSocket %s: ", wsName), t);}

    public WebSocket getWebSocket(){ return this.webSocket;}

}
