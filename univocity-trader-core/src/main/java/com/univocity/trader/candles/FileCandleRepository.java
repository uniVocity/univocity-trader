package com.univocity.trader.candles;

import com.univocity.parsers.common.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.io.*;
import java.time.*;
import java.util.*;

import static com.univocity.trader.candles.Candle.*;

public class FileCandleRepository extends CandleRepository {

	private static final Logger log = LoggerFactory.getLogger(FileCandleRepository.class);

	private final RepositoryDir repositoryDir;
	private final RowFormat<?, ?> rowFormat;


	FileCandleRepository(RepositoryDir repositoryDir, RowFormat<?, ?> rowFormat) {
		this.repositoryDir = repositoryDir;
		this.rowFormat = rowFormat;
	}

	@Override
	public boolean addToHistory(String symbol, PreciseCandle tick, boolean initializing) {
		throw new UnsupportedOperationException("Can't store candles to local files (it's unreliable). Use a database for that.");
	}

	@Override
	String buildCandleQuery(String symbol, Instant from, Instant to) {
		String start = "";
		if (from != null) {
			start = " from " + getFormattedDateTimeWithYear(from.toEpochMilli());
		}
		String end = "";
		if (to != null) {
			end = " until " + getFormattedDateTimeWithYear(to.toEpochMilli());
		}
		return "Parse " + symbol + " candles" + start + end;
	}

	@Override
	public Set<String> getKnownSymbols() {
		return repositoryDir.entries().keySet();
	}

	@Override
	protected long performCandleCounting(String symbol, Instant from, Instant to) {
		return loadCandles(symbol, null, from, to, null);
	}

	@Override
	protected long loadCandles(String symbol, String query, Instant from, Instant to, Collection<Candle> out) {
		AbstractParser<?> parser = prepareToParse(symbol, from, to, out);
		long count = 0;
		final long end = to == null ? Long.MAX_VALUE : to.toEpochMilli();
		try {
			String[] row;
			while ((row = parser.parseNext()) != null) {
				Candle candle = rowFormat.toCandle(row);
				if (candle != null) {
					storeCandle(symbol, from, to, out, candle);
					count++;
					if (candle.closeTime >= end) {
						break;
					}
				}
			}
		} finally {
			parser.stopParsing();
		}

		return count;
	}

	private AbstractParser<?> prepareToParse(String symbol, Instant from, Instant to, Collection<Candle> out) {
		AbstractParser<?> parser = rowFormat.createParser();

		Reader input = repositoryDir.readEntry(symbol);
		parser.beginParsing(input);

		if (from != null) {
			long start = from.toEpochMilli();
			long open = 0;

			Candle candle = null;

			String[] row;
			while (open < start && (row = parser.parseNext()) != null) {
				candle = rowFormat.toCandle(row);
				if (candle != null) {
					open = candle.openTime;
				}
			}

			if (out != null && candle != null) {
				storeCandle(symbol, from, to, out, candle);
			}
		}

		return parser;
	}
}
