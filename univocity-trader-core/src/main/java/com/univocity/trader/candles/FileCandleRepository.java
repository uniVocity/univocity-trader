package com.univocity.trader.candles;

import com.univocity.parsers.common.*;
import com.univocity.trader.utils.*;
import org.apache.commons.io.input.*;
import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;

import static com.univocity.trader.candles.Candle.*;

public class FileCandleRepository extends CandleRepository {

	private static final Logger log = LoggerFactory.getLogger(FileCandleRepository.class);

	private final RepositoryDir repositoryDir;
	private final RowFormat<?, ?> rowFormat;


	public FileCandleRepository(RepositoryDir repositoryDir, RowFormat<?, ?> rowFormat) {
		this.repositoryDir = repositoryDir;
		this.rowFormat = rowFormat;
	}

	public RepositoryDir getRepositoryDir() {
		return repositoryDir;
	}

	public RowFormat<?, ?> getRowFormat() {
		return rowFormat;
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
		AbstractParser<?> parser = null;
		long count = 0;

		try {
			parser = prepareToParse(symbol, from, to, out);

			final long end = to == null ? Long.MAX_VALUE : to.toEpochMilli();

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
			if (parser != null) {
				parser.stopParsing();
			}
		}

		return count;
	}

	private AbstractParser<?> beginParsing(Reader input) {
		AbstractParser<?> parser = rowFormat.createParser();
		parser.beginParsing(input);
		return parser;
	}

	private AbstractParser<?> beginParsing(String symbol) {
		Reader input = repositoryDir.readEntry(symbol);
		return beginParsing(input);
	}

	private AbstractParser<?> prepareToParse(String symbol, Instant from, Instant to, Collection<Candle> out) {
		AbstractParser<?> parser = beginParsing(symbol);

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

	@Override
	public Candle lastCandle(String symbol) {
		File file = repositoryDir.entries().get(symbol);

		AbstractParser<?> parser = null;
		Candle candle = null;

		try (ReversedLinesFileReader reader = new ReversedLinesFileReader(file, StandardCharsets.UTF_8)) {
			String headerRow = "";
			if (rowFormat.hasHeaders) {
				parser = beginParsing(symbol);
				parser.getContext().headers();
				headerRow = parser.getContext().currentParsedContent();
				parser.stopParsing();
			}

			String line;
			do {
				line = reader.readLine();
				if (line != null) {
					Reader input = new StringReader(headerRow + line);
					if (parser == null) {
						parser = rowFormat.createParser();
					}
					parser.beginParsing(input);
					candle = firstCandle(parser);
				}
			} while (candle == null && line != null);
		} catch (Exception e) {
			log.warn("Error loading last candle of " + symbol + " from " + file);
		} finally {
			if (parser != null) {
				parser.stopParsing();
			}
		}
		return candle;
	}

	@Override
	public boolean isWritingSupported() {
		return false;
	}

	@Override
	public Candle firstCandle(String symbol) {
		AbstractParser<?> parser = null;
		try {
			parser = beginParsing(symbol);
			return firstCandle(parser);
		} finally {
			if (parser != null) {
				parser.stopParsing();
			}
		}
	}

	private Candle firstCandle(AbstractParser<?> parser) {
		Candle candle = null;
		String[] row;
		do {
			row = parser.parseNext();
			if (row != null) {
				candle = rowFormat.toCandle(row);
			}
		} while (candle == null && row != null);

		return candle;
	}
}
