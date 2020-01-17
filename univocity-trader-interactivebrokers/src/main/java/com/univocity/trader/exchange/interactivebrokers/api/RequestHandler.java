package com.univocity.trader.exchange.interactivebrokers.api;

import com.univocity.trader.candles.*;
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
	private Map<Integer, IBIncomingCandles> activeFeeds = new ConcurrentHashMap<>();

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
		if (!awaitingResponse.remove(requestId)) {
			log.warn("No response received for request id {}", requestId);
			synchronized (syncLock) {
				syncLock.notifyAll();
			}
		} else {
			log.debug("Response finalized for request id {}", requestId);
		}
	}

	void closeOpenFeed(int requestId) {
		IBIncomingCandles feed = activeFeeds.remove(requestId);
		if (feed != null) {
			log.info("Closing active feed opened by request {}", requestId);
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
				if (messageCode == 507) { //bad message length, connection issues (still connected though).
					cancelAllPendingRequests();
				}
			}
		} else {
			twsDisconnected = false;
			log.warn("Error received for request ID [{}]: {} (Error code: {})", requestId, message, messageCode);
			cancelPendingRequest(requestId);
		}
	}

	private void cancelPendingRequest(int requestId) {
		try {
			pendingRequests.remove(requestId);
			closeOpenFeed(requestId);
		} finally {
			if (awaitingResponse.remove(requestId)) {
				synchronized (syncLock) {
					syncLock.notifyAll();
				}
			}
		}
	}

	private void cancelAllPendingRequests() {
		Integer[] requestIds = pendingRequests.keySet().toArray(new Integer[0]);
		Arrays.sort(requestIds);
		log.warn("Cancelling all pending requests due to connection issues. Request IDs: {}", Arrays.toString(requestIds));

		for (Integer requestId : requestIds) {
			cancelPendingRequest(requestId);
		}

	}

	void handleResponse(int requestId, Object responseToConsume, Supplier<String> messageLogger) {
		handleResponse(requestId, responseToConsume,
				(consumer) -> handleMessageResponse(consumer, responseToConsume, messageLogger));
	}

	<I, O> void handleResponse(int requestId, boolean done, Collection<I> responseToConsume, Function<I, O> objectTransformation, Function<I, String> messageLogger) {
		handleResponse(requestId, responseToConsume,
				(consumer) -> processCollection(done, responseToConsume, consumer, objectTransformation, messageLogger));
	}

	<I, O> void handleResponse(int requestId, Collection<I> responseToConsume, Function<I, O> objectTransformation, Function<I, String> messageLogger) {
		handleResponse(requestId, responseToConsume,
				(consumer) -> processCollection(true, responseToConsume, consumer, objectTransformation, messageLogger));
	}

	private void handleMessageResponse(Consumer consumer, Object responseToConsume, Supplier<String> messageLogger) {
		try {
			consumer.accept(responseToConsume);
		} catch (Exception e) {
			log.error("Error processing response for request ID " + requestId + ". Received: " + messageLogger.get(), e);
		}
	}

	private void handleResponse(int requestId, Object responseToConsume, Consumer<Consumer> handler) {
		try {
			Consumer consumer = pendingRequests.get(requestId);
			if (consumer == null) {
				log.error("No consumer for response received for request ID {}. Response: {}", requestId, responseToConsume);
				return;
			}

			handler.accept(consumer);
		} finally {
			awaitingResponse.remove(requestId);
			synchronized (syncLock) {
				syncLock.notifyAll();
			}
		}
	}

	private <I, O> void processCollection(boolean done, Collection<I> responseToConsume, Consumer consumer, Function<I, O> objectTransformation, Function<I, String> messageLogger) {
		for (I i : responseToConsume) {
			processElement(i, consumer, objectTransformation, messageLogger);
		}
		if (done) {
			consumer.accept(null); //notifies end of list.
		}
	}

	private <I, O> void processElement(I i, Consumer consumer, Function<I, O> objectTransformation, Function<I, String> messageLogger) {
		try {
			Object o = i;
			if (objectTransformation != null) {
				o = objectTransformation.apply(i);
			}
			consumer.accept(o);

		} catch (Exception e) {
			log.error("Error processing response for request ID " + requestId + ". Received: " + messageLogger.apply(i), e);
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

	public IBIncomingCandles openFeed(Function<Consumer<Candle>, Integer> request) {
		return doOpenFeed(request, null);
	}

	public IBIncomingCandles openFeed(Function<Consumer<Candle>, Integer> request, Consumer<Integer> cancelRequestHandler) {
		return doOpenFeed(request, cancelRequestHandler);
	}

	private IBIncomingCandles doOpenFeed(Function<Consumer<Candle>, Integer> request, Consumer<Integer> cancelRequestHandler) {
		IBIncomingCandles out = new IBIncomingCandles();

		int[] reqId = new int[1];
		reqId[0] = request.apply((candle) -> {
					if (!out.consumerStopped()) {
						if (candle == null) { //null candle must be sent manually after processing a fixed list of ticks.
							closeOpenFeed(reqId[0]);
						} else {
							out.add(candle);
						}
					} else if (cancelRequestHandler != null) {
						if (reqId[0] != 0) {
							log.warn("Cancelling feed opened by request {}. Consumer stopped reading from it.", reqId[0]);
							cancelRequestHandler.accept(reqId[0]);
							reqId[0] = 0;
						}
					}
				}
		);
		activeFeeds.put(reqId[0], out);
		return out;
	}
}
