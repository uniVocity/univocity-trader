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
	private final DatabaseCandleRepository candleRepository;

	public MarketHistory(String symbol, DatabaseCandleRepository candleRepository) {
		this.symbol = symbol;
		this.candleRepository = candleRepository;
	}

	public final String getSymbol() {
		return symbol;
	}

	public final void simulate(Consumer<Candle> consumer, boolean cache) {
		simulate(consumer, null, null, cache);
	}

	public final void simulate(Consumer<Candle> consumer, Instant from, boolean cache) {
		simulate(consumer, from, null, cache);
	}

	public final void simulate(Consumer<Candle> consumer, Instant from, Instant to, boolean cache) {
		Enumeration<Candle> result = candleRepository.iterate(symbol, from, to, cache);
		Candle candle;
		if (log.isTraceEnabled()) {
			final long start = System.currentTimeMillis();
			int count = 0;

			while (result.hasMoreElements() && (candle = result.nextElement()) != null) {
				consumer.accept(candle);
				count++;
			}
			log.trace("Processed all {} candles of {} in {} seconds", count, symbol, (System.currentTimeMillis() - start) / 1000.0);
		} else {
			while (result.hasMoreElements() && (candle = result.nextElement()) != null) {
				consumer.accept(candle);
			}
		}

	}

	public final Candle last() {
		return candleRepository.lastCandle(symbol);
	}

	public final  long size() {
		return candleRepository.countCandles(symbol);
	}
}
