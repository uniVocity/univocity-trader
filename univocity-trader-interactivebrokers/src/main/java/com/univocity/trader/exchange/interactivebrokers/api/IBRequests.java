package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.util.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class IBRequests {

	private static final Logger log = LoggerFactory.getLogger(IBRequests.class);

	private final EJavaSignal signal = new EJavaSignal();

	final RequestHandler requestHandler = new RequestHandler(this::reconnect);
	private final ResponseProcessor responseProcessor = new ResponseProcessor(requestHandler);

	protected EClientSocket client;

	private String ip;
	private int port;
	private int clientID;
	private String optionalCapabilities;
	private boolean reconnecting = false;

	public IBRequests(String ip, int port, int clientID, String optionalCapabilities) {
		this.ip = ip;
		this.port = port;
		this.clientID = clientID;
		this.optionalCapabilities = optionalCapabilities;
		connect();
	}

	private synchronized EClientSocket getClient() {
		if (client == null) {
			client = new EClientSocket(responseProcessor, signal);
			client.optionalCapabilities(optionalCapabilities);
			client.eConnect(ip, port, clientID);
			if (client.isConnected()) {
				log.info("Connected to TWS server (version {}})", client.serverVersion());
			} else {
				throw new IllegalStateException("Could not connect to TWS. Make sure it's running on " + (StringUtils.isBlank(ip) ? "localhost" : ip) + ":" + port);
			}
		}
		return client;
	}

	public void reconnect() {
		if(!reconnecting){
			synchronized (this){
				if(!reconnecting) {
					try {
						this.reconnecting = true;
						disconnect();
						log.info("Reconnecting to TWS");
						connect();
					} finally {
						reconnecting = false;
					}
				}
			}
		}
	}

	private synchronized void connect() {
		EReader reader = new EReader(getClient(), signal);
		reader.start();

		boolean[] ready = new boolean[]{false};

		new Thread(() -> {
			Thread.currentThread().setName("IB live stream");
			while (client != null && client.isConnected()) {
				ready[0] = true;
				signal.waitForSignal();
				try {
					reader.processMsgs();
				} catch (Exception e) {
					log.error("Error processing messages", e);
				}
			}
			log.warn("IB live stream stopped");
		}).start();

		while (!ready[0]) {
			Thread.yield();
		}
		log.info("Connected to TWS.");
	}

	public void disconnect() {
		if(client != null) {
			log.warn("Disconnecting from IB live stream");
			client.cancelRealTimeBars(3001);
			client.eDisconnect();
			client = null;
		}
	}

	public int submitRequest(String description, Consumer resultConsumer, Consumer<Integer> action) {
		int requestId = requestHandler.prepareRequest(resultConsumer);
		log.debug("New request [" + requestId + "]: " + description);
		action.accept(requestId);
		return requestId;
	}

	public void waitForResponse(int requestId) {
		waitForResponse(requestId, 10);
	}

	public void waitForResponse(int requestId, int maxSecondsToWait) {
		requestHandler.waitForResponse(requestId, maxSecondsToWait);
	}

	public void waitForResponses(Collection<Integer> requestIds) {
		waitForResponses(requestIds, 10);
	}

	public void waitForResponses(Collection<Integer> requestIds, int maxSecondsToWait) {
		requestHandler.waitForResponses(requestIds, maxSecondsToWait);
	}
}
