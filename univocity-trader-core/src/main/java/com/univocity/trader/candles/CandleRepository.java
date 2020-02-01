package com.univocity.trader.candles;

import com.univocity.trader.config.*;
import org.slf4j.*;
import org.springframework.jdbc.core.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class CandleRepository {
	private static final Logger log = LoggerFactory.getLogger(CandleRepository.class);
	private String databaseName;
	private static final String INSERT = "INSERT INTO candle (symbol,open_time,close_time,open,high,low,close,volume) VALUES (?,?,?,?,?,?,?,?)";
	private static final RowMapper<Candle> CANDLE_MAPPER = (rs, rowNum) -> {
		Candle out = new Candle(rs.getLong(1), rs.getLong(2), rs.getDouble(3), rs.getDouble(4), rs.getDouble(5),
				rs.getDouble(6), rs.getDouble(7));
		return out;
	};

	protected final ConcurrentHashMap<String, Collection<Candle>> cachedResults = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Long> candleCounts = new ConcurrentHashMap<>();
	private final ThreadLocal<JdbcTemplate> db;

	public CandleRepository(DatabaseConfiguration config) {
		this.db = ThreadLocal.withInitial(() -> new JdbcTemplate(config.dataSource()));
	}

	public JdbcTemplate db() {
		return db.get();
	}

	private String buildCandleQuery(String symbol) {
		return "SELECT open_time, close_time, open, high, low, close, volume FROM candle WHERE symbol = '" + symbol
				+ "'";
	}

	private PreparedStatement prepareInsert(PreparedStatement ps, String symbol, PreciseCandle tick)
			throws SQLException {
		ps.setObject(1, symbol);
		ps.setObject(2, tick.openTime);
		ps.setObject(3, tick.closeTime);
		ps.setObject(4, tick.open);
		ps.setObject(5, tick.high);
		ps.setObject(6, tick.low);
		ps.setObject(7, tick.close);
		ps.setObject(8, tick.volume);
		return ps;
	}

	private final ConcurrentHashMap<String, long[]> recentCandles = new ConcurrentHashMap<>();

	public boolean addToHistory(String symbol, PreciseCandle tick, boolean initializing) {
		candleCounts.clear();
		try {
			long[] times = recentCandles.get(symbol);
			if (times != null && times[0] == tick.openTime && times[1] == tick.closeTime) {
				return false; // duplicate, skip
			} else {
				if (times == null) {
					times = new long[2];
					recentCandles.put(symbol, times);
				}
				times[0] = tick.openTime;
				times[1] = tick.closeTime;
			}

			if (db().execute(INSERT,
					(PreparedStatementCallback<Integer>) ps -> prepareInsert(ps, symbol, tick).executeUpdate()) == 0) {
				log.warn("Could not persist " + symbol + " Tick: " + tick);
				return false;
			}
		} catch (Exception ex) {
			if (ex.getMessage().contains("Duplicate entry")) {
				if (!initializing) {
					log.error("Skipping duplicate " + symbol + " Tick: " + tick);
				}
			} else {
				log.error("Error persisting " + symbol + " Tick: " + tick, ex);
			}
			return false;
		}
		return true;
	}

	protected Enumeration<Candle> cacheAndReturnResults(String symbol, String query, Instant from, Instant to,
			Collection<Candle> out) {
		cachedResults.put(symbol, out);
		return Collections.enumeration(out);
	}

	private Enumeration<Candle> toEnumeration(String symbol, String query, Instant from, Instant to,
			Runnable readingProcess, Collection<Candle> out, boolean[] ended) {
		if (!(out instanceof BlockingQueue)) {
			readingProcess.run();
			return cacheAndReturnResults(symbol, query, from, to, out);
		}

		final BlockingQueue<Candle> queue = (BlockingQueue<Candle>) out;
		new Thread(readingProcess).start();
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

	protected Enumeration<Candle> executeQuery(String symbol, String query, Instant from, Instant to,
			Collection<Candle> out) {
		boolean[] ended = new boolean[] { false };

		final long start = System.currentTimeMillis();
		Runnable readingProcess = () -> {
			Thread.currentThread().setName(symbol + " candle reader");
			log.debug("Executing SQL query: [{}]", query);
			int count = 0;

			try (Connection c = db().getDataSource().getConnection();
					final PreparedStatement s = c.prepareStatement(query);
					ResultSet rs = executeQuery(s)) {

				while (rs.next()) {
					Candle candle = CANDLE_MAPPER.mapRow(rs, 0);
					storeCandle(symbol, from, to, out, candle);
					count++;
				}
			} catch (SQLException e) {
				log.error("Error reading " + symbol + " Candle from db().", e);
			} finally {
				log.trace("Read all {} candles of {} in {} seconds", count, symbol,
						(System.currentTimeMillis() - start) / 1000.0);
				ended(symbol, ended);
			}
		};

		return toEnumeration(symbol, query, from, to, readingProcess, out, ended);
	}

	private boolean isDatabaseMySQL() {
		if (databaseName == null) {
			synchronized (this) {
				try {
					databaseName = db().execute((ConnectionCallback<String>) connection -> connection.getMetaData()
							.getDatabaseProductName());
				} catch (Exception e) {
					log.warn("Unable to determine database name", e);
				}
				if (databaseName == null) {
					databaseName = "";
				}
				databaseName = databaseName.trim().toLowerCase();
			}
		}
		return databaseName.contains("mysql") || databaseName.contains("maria");
	}

	private ResultSet executeQuery(PreparedStatement s) throws SQLException {
		if (isDatabaseMySQL()) {
			// ensures MySQL's JDBC driver won't run out of memory if querying a large
			// number of candles
			s.setFetchSize(Integer.MIN_VALUE);
		}
		return s.executeQuery();
	}

	public void clearCaches() {
		cachedResults.clear();
	}

	public void evictFromCache(String symbol) {
		log.trace("Evicting cached candles of {}", symbol);
		Collection<Candle> candles = cachedResults.remove(symbol);
		if (candles != null) {
			candles.clear();
		}
	}

	public String narrowQueryToTimeInterval(String query, Long from, Long to) {
		if (from != null || to != null) {
			query += " AND ";

			if (from != null) {
				query += "open_time >= " + from;
			}

			if (to != null) {
				if (from != null) {
					query += " AND ";
				}
				query += "close_time <= " + to;
			}
		}
		return query;
	}

	public String narrowQueryToTimeInterval(String query, Instant from, Instant to) {
		return narrowQueryToTimeInterval(query, from == null ? null : from.toEpochMilli(),
				to == null ? null : to.toEpochMilli());
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
					log.error("Error waiting for cached result of query " + query, e);
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

	public Enumeration<Candle> iterate(String symbol, Instant from, Instant to, boolean cache) {
		String query = buildCandleQuery(symbol);
		query = narrowQueryToTimeInterval(query, from, to);
		query += " ORDER BY open_time";

		Collection<Candle> out;

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

	long count(String query, Object... params) {
		Long result = db().queryForObject(query, params, Long.class);
		if (result == null) {
			return 0;
		}
		return result;
	}

	public Set<String> getKnownSymbols() {
		return new TreeSet<>(db().queryForList("SELECT DISTINCT symbol FROM candle", String.class));
	}

	public final long countCandles(String symbol, Instant from, Instant to) {
		String key = symbol + toMs(from) + "_" + toMs(to);
		return candleCounts.computeIfAbsent(key, (k) -> performCandleCounting(symbol, from, to));
	}

	private Long toMs(Instant instant) {
		if (instant == null) {
			return 0L;
		}
		return instant.toEpochMilli();
	}

	protected long performCandleCounting(String symbol, Instant from, Instant to) {
		String query = "SELECT COUNT(*) FROM candle WHERE symbol = ?";
		query = narrowQueryToTimeInterval(query, from, to);
		return count(query, symbol);
	}

	public long countCandles(String symbol) {
		return countCandles(symbol, null, null);
	}

	public Candle lastCandle(String symbol) {
		String query = buildCandleQuery(symbol);
		query += " ORDER BY close_time DESC LIMIT 1";
		return db().queryForObject(query, CANDLE_MAPPER);
	}
}
