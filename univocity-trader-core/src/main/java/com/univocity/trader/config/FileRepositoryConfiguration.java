package com.univocity.trader.config;

import com.univocity.parsers.csv.*;
import com.univocity.parsers.fixed.*;
import com.univocity.parsers.tsv.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.candles.builders.*;
import com.univocity.trader.utils.*;

import java.io.*;
import java.nio.file.*;

public class FileRepositoryConfiguration implements ConfigurationGroup {

	private final RepositoryDir repositoryDir = new RepositoryDir();
	private RowFormat<?, ?> rowFormat;

	@Override
	public void readProperties(PropertyBasedConfiguration properties) {
	}


	@Override
	public boolean isConfigured() {
		return rowFormat != null && repositoryDir.isConfigured();
	}

	public RepositoryDir dir() {
		return repositoryDir;
	}

	public FileRepositoryConfiguration dir(File signalRepositoryDir) {
		repositoryDir.set(signalRepositoryDir);
		return this;
	}

	public FileRepositoryConfiguration dir(Path signalRepositoryDir) {
		repositoryDir.set(signalRepositoryDir);
		return this;
	}

	public <T> ColumnSelectionType<CsvParserSettings> csv() {
		return RowFormat.csv(this::rowFormat);
	}

	public <T> ColumnSelectionType<TsvParserSettings> tsv() {
		return RowFormat.tsv(this::rowFormat);
	}

	public <T> ColumnSelectionType<FixedWidthParserSettings> fixedWidth(FixedWidthFields fixedWidthFields) {
		return RowFormat.fixedWidth(fixedWidthFields, this::rowFormat);
	}

	public FileRepositoryConfiguration rowFormat(RowFormat<?, ?> rowFormat) {
		this.rowFormat = rowFormat;
		return this;
	}

	public RowFormat<?, ?> rowFormat() {
		return rowFormat;
	}
}