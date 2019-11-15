package com.univocity.trader.simulation;

import com.univocity.trader.candles.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class MarketHistory {

	private static final Logger log = LoggerFactory.getLogger(MarketHistory.class);
	public final String symbol;

	public MarketHistory(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public void simulate(Consumer<Candle> consumer, boolean cache) {
		simulate(consumer, null, null, cache);
	}

	public void simulate(Consumer<Candle> consumer, Instant from, boolean cache) {
		simulate(consumer, from, null, cache);
	}

	public void simulate(Consumer<Candle> consumer, Instant from, Instant to, boolean cache) {
		Enumeration<Candle> result = CandleRepository.iterate(symbol, from, to, cache);
		final long start = System.currentTimeMillis();
		int count = 0;
		while (result.hasMoreElements()) {
			Candle candle = result.nextElement();
			if (candle == null) {
				break;
			}
			consumer.accept(candle);
			count++;
		}
		log.trace("Processed all {} candles of {} in {} seconds", count, symbol, (System.currentTimeMillis() - start) / 1000.0);
	}

	public Candle last() {
		return CandleRepository.lastCandle(symbol);
	}

	public long size() {
		return CandleRepository.countCandles(symbol);
	}
}
