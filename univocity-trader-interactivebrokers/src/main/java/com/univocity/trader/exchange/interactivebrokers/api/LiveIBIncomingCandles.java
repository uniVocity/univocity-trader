package com.univocity.trader.exchange.interactivebrokers.api;

import com.univocity.trader.candles.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class LiveIBIncomingCandles extends IBIncomingCandles {

	private final TickConsumer<Candle> candleConsumer;
	public final String symbol;
	public Candle latestCandle;

	public LiveIBIncomingCandles(String symbol, TickConsumer<Candle> candleConsumer) {
		this.candleConsumer = candleConsumer;
		this.symbol = symbol;
	}


	@Override
	public void add(Candle candle) {
		adjustTime(candle);
		candleConsumer.tickReceived(symbol, candle);
		this.latestCandle = candle;
	}

	public final double latestPrice() {
		return latestCandle == null ? 0.0 : latestCandle.close;
	}

	@Override
	public void stopProducing() {
		try {
			super.stopProducing();
		} finally {
			candleConsumer.streamClosed();
		}
	}
}
