package com.univocity.trader.exchange.interactivebrokers.api;

import com.ib.client.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.util.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
abstract class IBRequests {

	private static final Logger log = LoggerFactory.getLogger(IBRequests.class);

	protected EClientSocket client;
	private EJavaSignal signal;
	private EReader reader;

	final RequestHandler requestHandler;
	final ResponseProcessor responseProcessor;


	final String ip;
	final int port;
	final int clientID;
	final String optionalCapabilities;
	private boolean reconnecting = false;

	public IBRequests(String ip, int port, int clientID, String optionalCapabilities, Runnable reconnectionProcess) {
		this.ip = ip;
		this.port = port;
		this.clientID = clientID;
		this.optionalCapabilities = optionalCapabilities;

		this.requestHandler = new RequestHandler(reconnectionProcess);
		this.responseProcessor = new ResponseProcessor(requestHandler);
		connect();
	}

	abstract IBRequests newInstance(IBRequests oldInstance);

	private EJavaSignal getSignal() {
		if (signal == null) {
			synchronized (this) {
				if (signal == null) {
					signal = new EJavaSignal();
				}
			}
		}
		return signal;
	}

	private EClientSocket getClient() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					signal = null;
					client = new EClientSocket(responseProcessor, getSignal());
					client.optionalCapabilities(optionalCapabilities);
					client.eConnect(ip, port, clientID);
					if (client.isConnected()) {
						log.info("Connected to TWS server (version {}})", client.serverVersion());
					} else {
						throw new IllegalStateException("Could not connect to TWS. Make sure it's running on " + (StringUtils.isBlank(ip) ? "localhost" : ip) + ":" + port);
					}
				}
			}
		}
		return client;
	}

	public static IBRequests reconnect(IBRequests ibRequests) {
		if (ibRequests != null && !ibRequests.reconnecting) {
			synchronized (IBRequests.class) {
				if (!ibRequests.reconnecting) {
					ibRequests.reconnecting = true;
					return ibRequests.newInstance(ibRequests);
				}
			}
		}
		return ibRequests;
	}

	private EReader getReader() {
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new EReader(getClient(), getSignal());
					reader.start();
				}
			}
		}
		return reader;
	}

	private synchronized void connect() {
		boolean[] ready = new boolean[]{false};
		getReader();

		new Thread(() -> {
			Thread.currentThread().setName("IB live stream");
			while (getClient().isConnected()) {
				ready[0] = true;
				getSignal().waitForSignal();
				try {
					getReader().processMsgs();
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

	public synchronized void disconnect() {
		if (client != null) {
			log.warn("Disconnecting from IB live stream");
			client.cancelRealTimeBars(3001);

			if (signal != null) {
				try {
					signal.issueSignal();
				} catch (Exception e) {
					//ignore. Don't care.
				}
			}

			if (reader != null) {
				try {
					reader.interrupt();
					reader = null;
				} catch (Exception e) {
					//ignore. Don't care.
				}
			}

			try {
				client.eDisconnect();
			} catch (Exception e) {
				//ignore. Don't care.
			}

			client = null;
			signal = null;
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
