package com.univocity.trader.utils;

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
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class IncomingCandles<T> implements Enumeration<T>, Iterable<T> {

	private final Queue<T> input = new ConcurrentLinkedQueue<>();
	private final Object lock = new Object();

	private boolean noMoreCandles = false;
	private boolean consuming = true;

	public void stopProducing() {
		noMoreCandles = true;
		notifyNotEmpty();
	}

	public void stopConsuming() {
		consuming = false;
		notifyNotEmpty();
	}

	public void add(T candle) {
		if (consuming) {
			input.offer(candle);
			notifyNotEmpty();
		}
	}

	@Override
	public boolean hasMoreElements() {
		return consuming && (!noMoreCandles || !input.isEmpty());
	}

	@Override
	public T nextElement() {
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
		while (consuming && input.isEmpty() && !noMoreCandles) {
			synchronized (lock) {
				try {
					lock.wait(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}


	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {
			T next;

			@Override
			public boolean hasNext() {
				if (next == null) {
					next = nextElement();
				}
				return next != null;
			}

			@Override
			public T next() {
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
