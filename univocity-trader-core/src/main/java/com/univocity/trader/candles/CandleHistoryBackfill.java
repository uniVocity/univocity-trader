package com.univocity.trader.candles;

import com.univocity.trader.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;

import static com.univocity.trader.candles.Candle.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class CandleHistoryBackfill {

	private static final Logger log = LoggerFactory.getLogger(CandleHistoryBackfill.class);
	private final DatabaseCandleRepository candleRepository;
	private boolean resumeBackfill = false;

	public CandleHistoryBackfill(DatabaseCandleRepository candleRepository) {
		this.candleRepository = candleRepository;
	}

	public boolean resumeBackfill() {
		return resumeBackfill;
	}

	public void resumeBackfill(boolean resumeBackfill) {
		this.resumeBackfill = resumeBackfill;
	}

	public <T> void fillHistory(Exchange<T, ?> exchange, String symbol, Instant from, Instant to, TimeInterval minGap) {
		long start = resumeIfPossible(symbol, from).toEpochMilli();
		long end = to.toEpochMilli();
		log.info("Refreshing history of {} from {} to {}.", symbol, getFormattedDateTimeWithYear(start), getFormattedDateTimeWithYear(end));
		IncomingCandles<T> ticks = exchange.getHistoricalTicks(symbol, minGap, start, end);
		persistIncomingCandles(exchange, ticks, symbol, start);
		log.info("{} history backfill process complete.", symbol);
	}

	private <T> void fillTickHistory(Exchange<T, ?> exchange, String symbol, Instant from, Instant to, TimeInterval minGap) {
		long end = resumeIfPossible(symbol, to).toEpochMilli(); //we go backwards from most recent date.
		final long stop = from.toEpochMilli();

		log.info("Refreshing tick history of {} from {} to {}.", symbol, getFormattedDateTimeWithYear(stop), getFormattedDateTimeWithYear(end));
		long start = end - TimeInterval.HOUR.ms;
		long lastRequest = -1;

		while (end > stop) {
			if (lastRequest != -1) {
				exchange.waitBeforeNextRequest(lastRequest);
			}
			lastRequest = System.currentTimeMillis();
			IncomingCandles<T> ticks = exchange.getHistoricalTicks(symbol, minGap, start, end);
			persistIncomingCandles(exchange, ticks, symbol, start);
			if (firstCandleReceived == null) {
				log.info("No more ticks available for {}.", symbol);
				break;
			}
			end = firstCandleReceived.closeTime;
			start = end - TimeInterval.HOUR.ms;
		}

		log.info("{} tick history backfill process complete.", symbol);
	}

	public <T> void fillHistoryGaps(Exchange<T, ?> exchange, String symbol, Instant from, TimeInterval minGap) {
		fillHistoryGaps(exchange, symbol, from, null, minGap);
	}

	private Instant resume(String symbol) {
		log.info("Checking if backfill process of {} can be resumed", symbol);
		var q = "SELECT max(close_time) FROM candle WHERE symbol = ? AND ts = (SELECT max(ts) FROM candle WHERE symbol = ? LIMIT 1)";
		Number closeOfLatestCandleLoaded = candleRepository.db().queryForObject(q, new Object[]{symbol, symbol}, Number.class);
		if (closeOfLatestCandleLoaded != null) {
			long ts = closeOfLatestCandleLoaded.longValue();
			return Instant.ofEpochMilli(ts);
		}
		return null;
	}

	private Instant resumeIfPossible(String symbol, Instant startingTime) {
		if (resumeBackfill) {
			Instant lastClose = resume(symbol);
			if (lastClose != null) {
				log.info("Resuming backfill process of symbol {} from {}", symbol, getFormattedDateTimeWithYear(lastClose.toEpochMilli()));
				return lastClose;
			}
		}
		return startingTime;
	}

	public <T> void fillHistoryGaps(Exchange<T, ?> exchange, String symbol, Instant from, Instant to, TimeInterval minGap) {
		to = to == null ? Instant.now() : to;
		final int limitPerRequest = exchange.historicalCandleCountLimit();
		if (limitPerRequest <= 0) {
			fillHistory(exchange, symbol, from, to, minGap);
			return;
		}

		if (minGap.ms <= 1) {
			fillTickHistory(exchange, symbol, from, to, minGap);
			return;
		}

		log.info("Looking for gaps in history of {} between {} and {}", symbol, getFormattedDateTimeWithYear(from.toEpochMilli()), getFormattedDateTimeWithYear(to.toEpochMilli()));

		IncomingCandles<T> ticks = exchange.getLatestTicks(symbol, minGap);
		if (persistIncomingCandles(exchange, ticks, symbol, from.toEpochMilli()) == 0) {
			throw new IllegalStateException("No recent history data received");
		}

		List<long[]> gaps = new ArrayList<>();

		long previous = from == null ? -1 : from.toEpochMilli();

		Enumeration<Candle> result = candleRepository.iterate(symbol, from, to, false);
		outer:
		while (result.hasMoreElements()) {
			Candle candle = result.nextElement();
			if (candle == null) {
				break;
			}

			long minute = candle.openTime;
			if (previous == -1) {
				previous = minute;
				continue;
			}
			final long gapStart = previous;
			long gap = minute - previous;
			if (gap > minGap.ms) {
				long limit = gap / minGap.ms;
				do {
					long start = previous;
					long end = minute;

					limit -= limitPerRequest;
					if (limit > 0) {
						end = start + (limitPerRequest * minGap.ms);
					}
					gaps.add(new long[]{start, end});
					previous = end;
				} while (limit > 0);
				log.warn("Historical data of {} has a gap of {} minutes between {} and {}", symbol, (gap / minGap.ms), getFormattedDateTimeWithYear(gapStart), getFormattedDateTimeWithYear(minute));
			}
			previous = minute;
		}

//		settings.removeIgnoredIntervals(gaps);

		if (!gaps.isEmpty()) {
//			if (gaps.size() > 30) {
//				log.warn("Too many gaps in history: {}. Will process the 30 most recent and ignore older transactions", gaps.size());
//				gaps = gaps.subList(gaps.size() - 30, gaps.size());
//			}
			Collections.reverse(gaps);
			fillGaps(exchange, symbol, minGap, gaps);
		}
		log.info("{} history backfill process complete", symbol);
	}

	private <T> void fillGaps(Exchange<T, ?> exchange, String symbol, TimeInterval minGap, List<long[]> gaps) {
		log.info("Filling {} gaps in history of {}", gaps.size(), symbol);

		int noDataCount = 0;
		for (long[] gap : gaps) {
			long start = gap[0];
			long end = gap[1];

			if (noDataCount > 20) {
				log.info("Aborting gap filling of {} as there is no data before {}", symbol, getFormattedDateTimeWithYear(start));
				return;
			}

			if (isKnownGap(symbol, start, end)) {
				noDataCount++;
				continue;
			}

			try {
				long lastRequest = System.currentTimeMillis();
				IncomingCandles<T> ticks = exchange.getHistoricalTicks(symbol.toUpperCase(), minGap, start, end);
				int count = 0;
				for (T tick : ticks) {
					count++;
					PreciseCandle candle = exchange.generatePreciseCandle(tick);
					candleRepository.addToHistory(symbol, candle, true);
				}

				if (count <= 2 && exchange.historicalCandleCountLimit() > 0) {
					noDataCount++;
//						log.info("No Candles found for {} between {} and {}", symbol, getFormattedDateTimeWithYear(start), getFormattedDateTimeWithYear(end));
					log.warn("Found a historical gap between {} and {}. Interval blacklisted.", getFormattedDateTimeWithYear(start), getFormattedDateTimeWithYear(end));
					addGap(symbol, start, end);
				} else {
					log.info("Loaded {} {} candles between {} and {}", count, symbol, getFormattedDateTimeWithYear(start), getFormattedDateTimeWithYear(end));
					noDataCount = 0;
				}

				if (ticks.consumerStopped()) {
					log.warn("Process interrupted while retrieving {} history between {} and {}", symbol, getFormattedDateTimeWithYear(start), getFormattedDateTimeWithYear(end));
				}

				exchange.waitBeforeNextRequest(lastRequest);
			} catch (Exception e) {
				log.error("Error retrieving history between {} and {}", start, end);
			}
		}
	}

	private boolean isKnownGap(String symbol, long start, long end) {
		return candleRepository.count("SELECT COUNT(*) FROM gap WHERE symbol = ? AND open_time = ? AND close_time = ?", symbol.toUpperCase(), start, end) > 0;
	}

	private void addGap(String symbol, long start, long end) {
		try {
			candleRepository.db().update("INSERT INTO gap VALUES (?,?,?)", symbol.toUpperCase(), start, end);
		} catch (Exception e) {
			log.error("Error persisting gap details: " + StringUtils.join(symbol, start, end), e);
		}
	}

	private PreciseCandle firstCandleReceived;

	private <T> int persistIncomingCandles(Exchange<T, ?> exchange, IncomingCandles<T> ticks, String symbol, long start) {
		firstCandleReceived = null;
		int persisted = 0;
		int received = 0;
		for (T tick : ticks) {
			PreciseCandle candle = exchange.generatePreciseCandle(tick);
			if (firstCandleReceived == null) {
				firstCandleReceived = candle;
			}
			if (candleRepository.addToHistory(symbol, candle, true)) {
				persisted++;
			}
			received++;
		}
		if (ticks.consumerStopped()) {
			log.warn("Process interrupted while retrieving {} history since {}", symbol, getFormattedDateTimeWithYear(start));
		}

		//all candles received are already in the database. Making a checkpoint so the backfill process can be
		//interrupted and resume from there.
		if (received > 0 && persisted == 0) {
			// deleting then inserting on purpose to avoid using a database-specific function to update the
			// timestamp is column `candle.ts`. This allows people to use the database they prefer.
			var delete = "DELETE FROM candle WHERE symbol = ? AND open_time = ? AND close_time = ?";
			candleRepository.db().update(delete, symbol, firstCandleReceived.openTime, firstCandleReceived.closeTime);

			if (candleRepository.addToHistory(symbol, firstCandleReceived, true)) {
				log.info("Made a checkpoint to resume future {} backfills from {}", symbol, getFormattedDateTimeWithYear(firstCandleReceived.closeTime));
			}
		}

		log.info("{} {} candles received, {} new candles added to history.", received, symbol, persisted);
		return received;
	}

}
