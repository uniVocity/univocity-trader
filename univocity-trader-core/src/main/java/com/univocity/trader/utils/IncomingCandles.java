package com.univocity.trader.utils;

import org.slf4j.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * A simple enumeration that receives candles from the exchange, which might come as a stream, and adds them to an
 * internal queue. The consumer of the enumeration will block if the queue becomes empty and the exchange is still
 * producing candles. The last call to {@link #nextElement()} might produce a {@code null} if the consumer
 * was blocked waiting for more candles, and the exchange finished adding candles to this internal queue.
 *
 * The {@link #iterator()} of this class ensures no {@code null} is produced so this can be used in for loops.
 *
 * If the consumer of candles returned by the exchange needs to stop processing them for some reason,
 * call {@link #stopConsuming()}.
 *
 * By default, after 30 seconds with an empty queue and without receiving anything since,
 * {@link #stopConsuming()} will be invoked automatically and abort the process. The {@link com.univocity.trader.Exchange}
 * implementation can check if {@link #consumerStopped()} produces {@code true} to close any open resources instead
 * of running indefinitely.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class IncomingCandles<T> implements Enumeration<T>, Iterable<T> {

	private static final Logger log = LoggerFactory.getLogger(IncomingCandles.class);

	private final Queue<T> input = new ConcurrentLinkedQueue<>();
	private final Object lock = new Object();

	private boolean noMoreCandles = false;
	private boolean consuming = true;

	private final long timeout;

	public IncomingCandles() {
		this(30_000);
	}

	public IncomingCandles(long timeout) {
		this.timeout = timeout;
	}

	public final void stopProducing() {
		noMoreCandles = true;
		notifyNotEmpty();
	}

	public final void stopConsuming() {
		consuming = false;
		notifyNotEmpty();
	}

	public final boolean consumerStopped() {
		return !consuming;
	}

	public void add(T candle) {
		if (consuming) {
			input.offer(candle);
			notifyNotEmpty();
		} else {
			log.warn("Consumer stopped. Rejected incoming candle {}", candle);
		}
	}

	@Override
	public final boolean hasMoreElements() {
		return consuming && (!noMoreCandles || !input.isEmpty());
	}

	@Override
	public final T nextElement() {
		T out = input.poll();
		while (out == null && consuming && !noMoreCandles) {
			waitWhileEmpty();
			out = input.poll();
		}
		return out;
	}

	private void notifyNotEmpty() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	private void waitWhileEmpty() {
		long waitStart = System.currentTimeMillis();
		while (consuming && input.isEmpty() && !noMoreCandles) {
			synchronized (lock) {
				try {
					lock.wait(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if (input.isEmpty() && System.currentTimeMillis() - waitStart >= timeout) {
				log.warn("Timeout waiting for candles to be returned from the exchange");
				stopConsuming();
			}
		}
	}


	@Override
	public final Iterator<T> iterator() {
		return new Iterator<>() {
			T next;

			@Override
			public final boolean hasNext() {
				if (next == null) {
					next = nextElement();
				}
				return next != null;
			}

			@Override
			public final T next() {
				if (next == null) {
					throw new IllegalStateException("Can't invoke next() without checking if there are more elements with hasNext()");
				}
				T out = next;
				next = null;
				return out;
			}
		};
	}

	public static <C> IncomingCandles<C> fromCollection(Collection<C> candles) {
		IncomingCandles out = new IncomingCandles();
		out.input.addAll(candles);
		out.stopProducing();
		return (IncomingCandles<C>) out;
	}
}
