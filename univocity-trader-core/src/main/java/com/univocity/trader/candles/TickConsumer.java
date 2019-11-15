package com.univocity.trader.candles;

public interface TickConsumer<T> {
	void tickReceived(String symbol, T tick);

	void streamError(Throwable cause);

	void streamClosed();
}