package com.univocity.trader.iqfeed.api.impl;

import com.univocity.trader.iqfeed.api.*;
import com.univocity.trader.iqfeed.api.constant.*;
import com.univocity.trader.iqfeed.api.domain.candles.*;
import com.univocity.trader.iqfeed.api.domain.request.*;
import org.asynchttpclient.*;
import org.asynchttpclient.ws.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

public class IQFeedApiWebSocketClientImpl implements IQFeedApiWebSocketClient, Closeable {

	private static final Logger log = LoggerFactory.getLogger(IQFeedApiWebSocketClientImpl.class);
	private IQFeedProcessor processor = null;
	private AsyncHttpClient client = null;
	private WebSocket webSocketClient = null;

	public IQFeedApiWebSocketClientImpl(AsyncHttpClient client, IQFeedApiWebSocketListener<?> listener) {
		if (client != null)
			try {
				this.client = client;
				listener.setProcessor(processor);
				webSocketClient = createNewWebSocket(listener);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
	}

	public void sendRequest(IQFeedHistoricalRequest request) {
		if (this.webSocketClient.isOpen()) {
			try {
				this.processor.setLatestRequest(request);
				this.webSocketClient.sendTextFrame(request.toString());
				log.info("Univocity-IQFeed OUT: " + request);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	public List<IQFeedCandle> getCandlestickBars(IQFeedHistoricalRequest request) {
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
	private WebSocket createNewWebSocket(IQFeedApiWebSocketListener<?> listener) {
		listener.setProcessor(processor);
		String streamingUrl = IQFeedApiConstants.HOST + ":" + IQFeedApiConstants.PORT;
		try {
			return client.prepareGet(streamingUrl)
					.execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()).get();
		} catch (Exception any) {
			log.error(String.format("Error while creating new websocket connection to %s", streamingUrl), any);
		}
		return null;
	}

	@Override
	public void close() {
	}

}
