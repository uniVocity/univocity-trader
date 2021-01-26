package com.univocity.trader.simulation;

import com.univocity.parsers.csv.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.utils.*;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class SignalRepository {
	private static final Logger log = LoggerFactory.getLogger(SignalRepository.class);
	private static final String[] HEADERS = {"OPEN_TIME", "CLOSE_TIME", "OPEN", "HIGH", "LOW", "CLOSE", "VOLUME", "SIGNAL"};

	private final RepositoryDir repositoryDir;
	private final Map<String, Map<Candle, Signal>> signals = new ConcurrentHashMap<>();


	public SignalRepository(File repositoryDir) {
		this(new RepositoryDir(repositoryDir));
	}

	public SignalRepository(Path repositoryDir) {
		this(new RepositoryDir(repositoryDir));
	}

	public SignalRepository(RepositoryDir repositoryDir) {
		this.repositoryDir = repositoryDir;
	}

	public SignalRepository(String symbol, Reader input) {
		this.repositoryDir = new RepositoryDir();
		load(symbol, input);
	}

	public void add(String symbol, Signal signal, Candle candle) {
		signals.computeIfAbsent(symbol, s -> new ConcurrentSkipListMap<>()).put(candle, signal);
	}

	public void save() {
		if (repositoryDir.isNotConfigured()) {
			log.warn("Not saving any signals. No repository dir defined");
			return;
		}
		log.info("Saving signals to repository dir: {}", repositoryDir);
		CsvWriterSettings settings = new CsvWriterSettings();
		settings.setHeaderWritingEnabled(true);
		settings.setHeaders(HEADERS);

		List<Thread> threads = new ArrayList<>();

		Map<String, File> entries = repositoryDir.entries();
		signals.forEach((symbol, v) ->
				threads.add(new Thread(() -> {
					File output = entries.get(symbol);
					CsvWriter writer = new CsvWriter(output, "UTF-8", settings);
					v.forEach((candle, signal) ->
							writer.writeRow(
									candle.openTime,
									candle.closeTime,
									candle.open,
									candle.high,
									candle.low,
									candle.close,
									candle.volume,
									signal
							)
					);
					writer.close();
				}))
		);
		runThreads(threads);
		log.info("All signals saved to repository dir: {}", repositoryDir);
	}

	public void load(String symbol, Reader input) {
		log.info("Loading signals from {} input", symbol);
		parseInput(symbol, input);
	}

	public void load() {
		if (repositoryDir.isNotConfigured()) {
			throw new IllegalStateException("Can't load signals. No repository dir defined");
		}
		log.info("Loading signals from repository dir: {}", repositoryDir);

		List<Thread> threads = new ArrayList<>();
		repositoryDir.readEntries().forEach((symbol, input) -> threads.add(new Thread(() -> parseInput(symbol, input))));

		runThreads(threads);
		log.info("All signals loaded from repository dir: {}", repositoryDir);
	}

	private void parseInput(String symbol, Reader reader) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.setLineSeparatorDetectionEnabled(true);
		settings.setHeaders(HEADERS);
		settings.setHeaderExtractionEnabled(true);

		CsvParser parser = new CsvParser(settings);

		try {
			parser.beginParsing(reader);
			com.univocity.parsers.common.record.Record record;
			while ((record = parser.parseNextRecord()) != null) {
				add(symbol, record.getValue("SIGNAL", Signal.class), new Candle(
						record.getLong("OPEN_TIME"),
						record.getLong("CLOSE_TIME"),
						record.getDouble("OPEN"),
						record.getDouble("HIGH"),
						record.getDouble("LOW"),
						record.getDouble("CLOSE"),
						record.getDouble("VOLUME")
				));
			}
		} catch (Exception e) {
			log.error("Error parsing " + symbol + " signal history ", e);
			parser.stopParsing();
		}
	}

	private void runThreads(List<Thread> threads) {
		threads.forEach(Thread::start);
		threads.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	Map<Candle, Signal> signalsFor(String symbol) {
		return signals.get(symbol);
	}

	Signal signalFor(String symbol, Candle candle) {
		Map<Candle, Signal> signals = signalsFor(symbol);
		if (signals == null) {
			throw new IllegalArgumentException("No signals for " + symbol + " in repository");
		}
		return signals.getOrDefault(candle, Signal.NEUTRAL);
	}

}
