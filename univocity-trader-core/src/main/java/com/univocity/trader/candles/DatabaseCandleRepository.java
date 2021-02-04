package com.univocity.trader.candles;

import com.univocity.trader.config.*;
import org.slf4j.*;
import org.springframework.dao.*;
import org.springframework.jdbc.core.*;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class DatabaseCandleRepository extends CandleRepository {
	private static final Logger log = LoggerFactory.getLogger(DatabaseCandleRepository.class);
	private String databaseName;
	private static final String INSERT = "INSERT INTO candle (symbol,open_time,close_time,open,high,low,close,volume) VALUES (?,?,?,?,?,?,?,?)";
	private static final RowMapper<Candle> CANDLE_MAPPER = (rs, rowNum) -> {
		Candle out = new Candle(
				rs.getLong(1),
				rs.getLong(2),
				rs.getDouble(3),
				rs.getDouble(4),
				rs.getDouble(5),
				rs.getDouble(6),
				rs.getDouble(7));
		return out;
	};

	private final ThreadLocal<JdbcTemplate> db;

	public DatabaseCandleRepository(DatabaseConfiguration config) {
		this.db = ThreadLocal.withInitial(() -> new JdbcTemplate(config.dataSource()));
	}

	public final JdbcTemplate db() {
		return db.get();
	}

	final String buildCandleQuery(String symbol, Instant from, Instant to) {
		String query = "SELECT open_time, close_time, open, high, low, close, volume FROM candle WHERE symbol = '" + symbol + "'";
		query = narrowQueryToTimeInterval(query, from, to);
		return query;
	}

	@Override
	public boolean isWritingSupported() {
		return true;
	}

	private PreparedStatement prepareInsert(PreparedStatement ps, String symbol, PreciseCandle tick) throws SQLException {
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

	private final ConcurrentHashMap<String, PreciseCandle> processingCandles = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, PreciseCandle> fullCandles = new ConcurrentHashMap<>();

	public boolean addToHistory(String symbol, PreciseCandle tick, boolean initializing) {
		candleCounts.clear();
		try {
			PreciseCandle processingCandle = processingCandles.get(symbol);
			if (processingCandle != null && processingCandle.openTime == tick.openTime && processingCandle.closeTime == tick.closeTime) {
				processingCandles.put(symbol, tick); //saving update of latest candle
				return true;
			} else {
				try {
					if (processingCandle != null) { //save fully populated candle
						if (db().execute(INSERT, (PreparedStatementCallback<Integer>) ps -> prepareInsert(ps, symbol, processingCandle).executeUpdate()) == 0) {
							log.warn("Could not persist " + symbol + " Tick: " + processingCandle);
							return false;
						}
						fullCandles.put(symbol, processingCandle);
					}
				} finally {
					processingCandles.put(symbol, tick);
				}
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

	@Override
	protected final long loadCandles(String symbol, String query, Instant from, Instant to, Collection<Candle> out) {
		long count = 0;
		boolean retry;
		try (Connection c = db().getDataSource().getConnection();
			 final PreparedStatement s = c.prepareStatement(query);
			 ResultSet rs = executeQuery(s)) {

			while (rs.next()) {
				Candle candle = CANDLE_MAPPER.mapRow(rs, 0);
				storeCandle(symbol, from, to, out, candle);
				count++;
			}

			retry = false;
		} catch (SQLException e) {
			log.error("Error reading " + symbol + " candles from database.", e);
			retry = e.getMessage().contains("Too many open files");
		}
		return retry ? -1 : count;
	}

	private boolean isDatabaseMySQL() {
		if (databaseName == null) {
			try {
				databaseName = db().execute((ConnectionCallback<String>) connection -> connection.getMetaData().getDatabaseProductName());
			} catch (Exception e) {
				log.warn("Unable to determine database name", e);
			}
			if (databaseName == null) {
				databaseName = "";
			}
			databaseName = databaseName.trim().toLowerCase();
		}
		return databaseName.contains("mysql") || databaseName.contains("maria");
	}

	private ResultSet executeQuery(PreparedStatement s) throws SQLException {
		if (isDatabaseMySQL()) {
			//ensures MySQL's JDBC driver won't run out of memory if querying a large number of candles
			s.setFetchSize(Integer.MIN_VALUE);
		}
		return s.executeQuery();
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
		return narrowQueryToTimeInterval(query, from == null ? null : from.toEpochMilli(), to == null ? null : to.toEpochMilli());
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

	protected long performCandleCounting(String symbol, Instant from, Instant to) {
		String query = "SELECT COUNT(*) FROM candle WHERE symbol = ?";
		query = narrowQueryToTimeInterval(query, from, to);
		return count(query, symbol);
	}

	public final Candle lastCandle(String symbol) {
		return loadCandle(symbol, "DESC");
	}

	public final Candle firstCandle(String symbol) {
		return loadCandle(symbol, "ASC");
	}

	private Candle loadCandle(String symbol, String ordering) {
		String query = buildCandleQuery(symbol, null, null);
		query += " ORDER BY close_time " + ordering + " LIMIT 1";
		try {
			return db().queryForObject(query, CANDLE_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public PreciseCandle lastFullCandle(String symbol) {
		return fullCandles.remove(symbol);
	}
}
