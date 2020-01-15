package com.univocity.trader.exchange.interactivebrokers.api;

import com.univocity.trader.candles.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
class RequestHandler {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);


	private final ThreadLocal<SimpleDateFormat> dateTimeFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd HH:mm:ss"));
	private final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));

	private final AtomicInteger requestId = new AtomicInteger(1);
	private final AtomicInteger orderId = new AtomicInteger(1);

	private Map<Integer, Consumer> pendingRequests = new ConcurrentHashMap<>();
	private Set<Integer> awaitingResponse = ConcurrentHashMap.newKeySet();
	private Map<Integer, IncomingCandles<?>> activeFeeds = new ConcurrentHashMap<>();

	private final Object syncLock = new Object();
	private boolean twsDisconnected = false;

	void setNextOrderId(int nextOrder) {
		orderId.set(nextOrder);
	}

	int prepareRequest(Consumer consumer) {
		if (twsDisconnected) {
			log.warn("Last request failed. Check if TWS is connected.");
		}
		int reqId = requestId.incrementAndGet();
		pendingRequests.put(reqId, consumer);
		awaitingResponse.add(reqId);
		return reqId;
	}

	void responseFinalized(int requestId) {
		pendingRequests.remove(requestId);
		if (awaitingResponse.remove(requestId)) {
			log.warn("No response received for request id {}", requestId);
			synchronized (syncLock) {
				syncLock.notifyAll();
			}
		} else {
			log.debug("Response finalized for request id {}", requestId);
		}
	}

	void closeOpenFeed(int requestId) {
		IncomingCandles<?> feed = activeFeeds.remove(requestId);
		if (feed != null) {
			feed.stopProducing();
		}
		responseFinalized(requestId);
	}

	void responseFinalizedWithError(int requestId, int messageCode, String message) {
		if (requestId < 0) {
			// refer to error message codes: https://interactivebrokers.github.io/tws-api/message_codes.html
			twsDisconnected = messageCode == 2103 || messageCode == 2105;
			if (twsDisconnected) {
				log.error("Server error: {} (Error code: {})", message, messageCode);
			} else {
				log.error("Server message: {} (Status code: {})", message, messageCode);
			}
		} else {
			twsDisconnected = false;
			log.warn("Error received for request ID [{}]: {} (Error code: {})", requestId, message, messageCode);
			pendingRequests.remove(requestId);
			if (awaitingResponse.remove(requestId)) {
				synchronized (syncLock) {
					syncLock.notifyAll();
				}
			}
		}
	}

	void handleResponse(int requestId, Object responseToConsume, Supplier<String> messageLogger) {
		try {
			Consumer consumer = pendingRequests.get(requestId);
			if (consumer == null) {
				log.error("No consumer for response received for request ID {}. Response: {}", requestId, responseToConsume);
				return;
			}
			consumer.accept(responseToConsume);
		} catch (Exception e) {
			log.error("Error processing response for request ID " + requestId + ". Received: " + messageLogger.get(), e);
		} finally {
			awaitingResponse.remove(requestId);
			synchronized (syncLock) {
				syncLock.notifyAll();
			}
		}
	}

	public void waitForResponses(Collection<Integer> requests, int maxSecondsToWait) {
		for (Integer i : requests) {
			waitForResponse(i, maxSecondsToWait);
		}
	}

	public void waitForResponse(int requestId, int maxSecondsToWait) {
		if (!awaitingResponse.contains(requestId)) {
			return;
		}

		long waitingSince = System.currentTimeMillis();
		synchronized (syncLock) {
			while (awaitingResponse.contains(requestId)) {
				try {
					syncLock.wait(2500);
					if (awaitingResponse.contains(requestId)) {
						if (System.currentTimeMillis() - waitingSince > maxSecondsToWait * 1000L) {
							log.warn("No response to request id {} after {} seconds", requestId, maxSecondsToWait);
							return;
						}
					}
				} catch (InterruptedException e) {
					log.error("Thread interrupted waiting for response to request id " + requestId, e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public String getFormattedDateTime(long time) {
		Calendar tmp = Calendar.getInstance();
		tmp.setTimeInMillis(time);
		return dateTimeFormat.get().format(time);
	}

	public long formattedDateToMillis(String date) {
		try {
			return dateTimeFormat.get().parse(date).getTime();
		} catch (ParseException e) {
			try {
				return dateFormat.get().parse(date).getTime();
			} catch (ParseException e1) {
				//ignore and let the first one go.
			}
			log.error("Unable to parse date " + date, e);
			return 0;
		}
	}

	public IncomingCandles<Candle> openFeed(Function<Consumer<Candle>, Integer> request) {
		IncomingCandles<Candle> out = new IncomingCandles<>();
		int reqId = request.apply(out::add);
		activeFeeds.put(reqId, out);
		return out;
	}
}
