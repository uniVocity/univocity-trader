package com.univocity.trader.candles;

import org.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.utils.RepositoryDir.*;

public abstract class CandleRepository {
	private static final Logger log = LoggerFactory.getLogger(CandleRepository.class);

	protected final ConcurrentHashMap<String, Collection<Candle>> cachedResults = new ConcurrentHashMap<>();
	protected final ConcurrentHashMap<String, Long> candleCounts = new ConcurrentHashMap<>();

	public CandleRepository() {

	}

	public abstract boolean isWritingSupported();

	public abstract boolean addToHistory(String symbol, PreciseCandle tick, boolean initializing);

	protected Enumeration<Candle> cacheAndReturnResults(String symbol, String query, Instant from, Instant to, Collection<Candle> out) {
		cachedResults.put(symbol, out);
		return Collections.enumeration(out);
	}

	final Enumeration<Candle> toEnumeration(String symbol, String query, Instant from, Instant to, Runnable readingProcess, Collection<Candle> out, boolean[] ended) {
		if (!(out instanceof BlockingQueue)) {
			readingProcess.run();
			return cacheAndReturnResults(symbol, query, from, to, out);
		}

		final BlockingQueue<Candle> queue = (BlockingQueue<Candle>) out;
		Thread process = new Thread(readingProcess);
		process.setDaemon(true);
		process.start();

		return new Enumeration<>() {
			@Override
			public boolean hasMoreElements() {
				return !ended[0] || !queue.isEmpty();
			}

			@Override
			public Candle nextElement() {
				while (hasMoreElements()) {
					Candle next = null;
					try {
						next = queue.poll(100L, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						log.error("Candle reading process interrupted", e);
						Thread.currentThread().interrupt();
					}
					if (next != null) {
						return next;
					}
				}
				return null;
			}
		};
	}

	protected void storeCandle(String symbol, Instant from, Instant to, Collection<Candle> out, Candle candle) {
		out.add(candle);
	}

	protected void ended(String symbol, boolean[] ended) {
		ended[0] = true;
	}

	protected abstract long loadCandles(String symbol, String query, Instant from, Instant to, Collection<Candle> out);

	public static String cleanSymbol(String symbol) {
		return symbol.replaceAll("[^A-Za-z0-9]", "");
	}

	protected Enumeration<Candle> executeQuery(String symbl, String query, Instant from, Instant to, Collection<Candle> out) {
		String symbol = cleanSymbol(symbl);
		boolean[] ended = new boolean[]{false};

		final long start = System.currentTimeMillis();
		Runnable readingProcess = () -> {
			Thread.currentThread().setName(symbol + " candle reader");
			log.debug("Fetching candles with: [{}]", query);

			long count = 0;
			try {
				boolean retry = false;
				do {
					count = 0;
					if (retry) {
						try {
							log.info("Waiting 5 seconds before retrying to read candles of {}", symbol);
							Thread.sleep(5_000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					count = loadCandles(symbol, query, from, to, out);
					retry = count < 0;
				} while (retry);
			} finally {
				log.trace("Read all {} candles of {} in {} seconds", count, symbol, (System.currentTimeMillis() - start) / 1000.0);
				ended(symbol, ended);
			}
		};

		return toEnumeration(symbol, query, from, to, readingProcess, out, ended);
	}

	public final void clearCaches() {
		cachedResults.clear();
	}

	public final void evictFromCache(String symbol) {
		if (log.isTraceEnabled()) {
			log.trace("Evicting cached candles of {}", symbol);
		}
		Collection<Candle> candles = cachedResults.remove(symbol);
		if (candles != null) {
			candles.clear();
		}
	}

	protected Enumeration<Candle> getCachedResults(String symbol, String query, Instant from, Instant to) {
		Collection<Candle> cachedResult;
		boolean waitForCache;
		synchronized (cachedResults) {
			cachedResult = cachedResults.get(symbol);
			if (cachedResult != null) {
				waitForCache = true;
			} else {
				cachedResults.put(symbol, Collections.emptyList());
				waitForCache = false;
			}
		}

		if (waitForCache) {
			while (cachedResult == Collections.EMPTY_LIST) {
				try {
					Thread.sleep(100);
					cachedResult = cachedResults.get(symbol);
				} catch (InterruptedException e) {
					log.error("Error waiting for cached result for: " + query, e);
					clearCaches();
					Thread.currentThread().interrupt();
				}
			}
			return Collections.enumeration(cachedResult);
		}
		return null;
	}

	protected Collection<Candle> getCacheStorage(long cacheSize) {
		return new ArrayList<>((int) cacheSize);
	}

	abstract String buildCandleQuery(String symbol, Instant from, Instant to);

	public final Enumeration<Candle> iterate(String symbol, Instant from, Instant to, boolean cache) {
		Collection<Candle> out;

		String query = buildCandleQuery(symbol, from, to) + " ORDER BY open_time";

		if (cache) {
			Enumeration<Candle> cached = getCachedResults(symbol, query, from, to);
			if (cached != null) {
				return cached;
			}
			long cacheSize = countCandles(symbol, from, to);
			out = getCacheStorage(cacheSize);
		} else {
			out = new ArrayBlockingQueue<>(5000) {
				public boolean add(Candle e) {
					try {
						super.put(e);
					} catch (InterruptedException ex) {
						log.error("Candle loading process interrupted", ex);
						Thread.currentThread().interrupt();
						return false;
					}
					return true;
				}
			};
		}

		return executeQuery(symbol, query, from, to, out);
	}

	public final long countCandles(String symbol) {
		return countCandles(symbol, null, null);
	}

	public final long countCandles(String symbol, Instant from, Instant to) {
		String key = symbol + toMs(from) + "_" + toMs(to);
		return candleCounts.computeIfAbsent(key, (k) -> performCandleCounting(symbol, from, to));
	}

	protected abstract long performCandleCounting(String symbol, Instant from, Instant to);

	public abstract Set<String> getKnownSymbols();

	final Long toMs(Instant instant) {
		if (instant == null) {
			return 0L;
		}
		return instant.toEpochMilli();
	}

	public abstract Candle lastCandle(String symbol);

	public abstract Candle firstCandle(String symbol);
}
