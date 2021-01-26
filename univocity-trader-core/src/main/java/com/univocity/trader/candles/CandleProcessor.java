package com.univocity.trader.candles;

import com.univocity.trader.*;
import com.univocity.trader.strategy.*;
import org.slf4j.*;

public final class CandleProcessor<T> {

	private static final Logger log = LoggerFactory.getLogger(CandleProcessor.class);

	private final Engine consumer;
	private final Exchange exchange;
	private final CandleRepository candleRepository;
	private final boolean processFullCandlesOnly;

	public CandleProcessor(CandleRepository candleRepository, Engine consumer, Exchange<T, ?> exchange, boolean processFullCandlesOnly) {
		this.candleRepository = candleRepository;
		this.consumer = consumer;
		this.exchange = exchange;
		this.processFullCandlesOnly = processFullCandlesOnly;
	}

	public void processCandle(Candle candle, boolean initializing) {
		try {
			consumer.process(candle, initializing);
		} catch (Exception e) {
			log.error("Error processing current candle:" + candle, e);
		}
	}

	public void processCandle(T realTimeTick, boolean initializing) {
		try {
			synchronized (consumer) {
				PreciseCandle tick = exchange.generatePreciseCandle(realTimeTick);
				if (!candleRepository.addToHistory(consumer.getSymbol(), tick, initializing)) {  //already processed, skip.
					return;
				}

				Candle candle;
				if (processFullCandlesOnly && !initializing && candleRepository.isWritingSupported()) {
					PreciseCandle fullCandle = ((DatabaseCandleRepository)candleRepository).lastFullCandle(consumer.getSymbol());
					if (fullCandle == null) {
						return;
					} else {
						candle = new Candle(fullCandle);
					}
				} else {
					candle = new Candle(tick);
				}

				processCandle(candle, initializing);
			}
		} catch (Exception e) {
			log.error("Error processing event:" + realTimeTick, e);
		}
	}
}

