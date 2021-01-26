package com.univocity.trader.utils;

import com.univocity.parsers.common.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static com.univocity.trader.candles.CandleRepository.*;

public class RepositoryDir {
	private File directory;

	public RepositoryDir() {
	}

	public RepositoryDir(File signalRepositoryDir) {
		set(signalRepositoryDir);
	}

	public RepositoryDir(Path signalRepositoryDir) {
		set(signalRepositoryDir);
	}

	public boolean isNotConfigured() {
		return !isConfigured();
	}

	public boolean isConfigured() {
		return directory != null;
	}

	public File get() {
		return directory;
	}

	public void set(File signalRepositoryDir) {
		if (signalRepositoryDir != null) {
			if (!signalRepositoryDir.exists()) {
				if (!signalRepositoryDir.mkdirs()) {
					throw new IllegalArgumentException("Can't create repository directory: " + signalRepositoryDir.getAbsolutePath());
				}
			} else if (!signalRepositoryDir.isDirectory()) {
				throw new IllegalArgumentException("Repository path is not a directory: " + signalRepositoryDir.getAbsolutePath());
			}
			this.directory = signalRepositoryDir;
		}
	}

	public void set(Path historySnapshotDir) {
		if (historySnapshotDir != null) {
			set(historySnapshotDir.toFile());
		}
	}

	public Map<String, File> entries() {
		Map<String, File> out = new TreeMap<>();

		File[] files = directory.listFiles();
		if (files == null) {
			return out;
		}
		for (File file : files) {
			if (file.toString().toLowerCase().endsWith(".csv")) {
				String filename = file.getName();
				String symbol = filename.substring(0, filename.length() - 4);
				symbol = cleanSymbol(symbol);
				out.put(symbol, file);
			}
		}
		return out;
	}

	public Reader readEntry(String name) {
		File file = entries().get(name);
		if (file == null) {
			throw new IllegalArgumentException("No file for '" + name + "' under directory '" + directory.getAbsolutePath() + "'. Available names: " + entries().keySet());
		}
		return readEntry(file);
	}

	private Reader readEntry(File file) {
		return ArgumentUtils.newReader(file, "UTF-8");
	}

	public Map<String, Reader> readEntries() {
		Map<String, Reader> out = new TreeMap<>();
		entries().forEach((symbol, file) -> out.put(symbol, readEntry(file)));
		return out;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RepositoryDir that = (RepositoryDir) o;

		return Objects.equals(directory, that.directory);
	}

	@Override
	public int hashCode() {
		return directory != null ? directory.hashCode() : 0;
	}

	@Override
	public String toString() {
		return directory == null ? "N/A" : directory.getAbsolutePath();
	}
}
