package com.univocity.trader.candles;

import com.univocity.parsers.common.*;
import com.univocity.parsers.csv.*;
import com.univocity.parsers.fixed.*;
import com.univocity.parsers.tsv.*;
import com.univocity.trader.candles.builders.*;
import org.slf4j.*;

import java.text.*;
import java.util.*;
import java.util.function.*;

public class RowFormat<T, F extends CommonParserSettings<?>> {

	private static final Logger log = LoggerFactory.getLogger(RowFormat.class);

	private ThreadLocal<SimpleDateFormat> dateTimeFormat;
	boolean hasHeaders = true;

	private T openDateTime;
	private T closeDateTime;
	private T open;
	private T high;
	private T low;
	private T close;
	private T volume;

	private final F parserSettings;

	private RowFormat(F inputFormat) {
		this.parserSettings = inputFormat;
	}

	public static <T> ColumnSelectionType<CsvParserSettings> csv() {
		return csv(null);
	}

	public static <T> ColumnSelectionType<CsvParserSettings> csv(Consumer<RowFormat<T, CsvParserSettings>> onBuild) {
		return new Builder<>(new RowFormat<>(new CsvParserSettings()), onBuild);
	}

	public static <T> ColumnSelectionType<TsvParserSettings> tsv() {
		return tsv(null);
	}

	public static <T> ColumnSelectionType<TsvParserSettings> tsv(Consumer<RowFormat<T, TsvParserSettings>> onBuild) {
		return new Builder<>(new RowFormat<>(new TsvParserSettings()), onBuild);
	}

	public static <T> ColumnSelectionType<FixedWidthParserSettings> fixedWidth(FixedWidthFields fixedWidthFields) {
		return fixedWidth(fixedWidthFields, null);
	}

	public static <T> ColumnSelectionType<FixedWidthParserSettings> fixedWidth(FixedWidthFields fixedWidthFields, Consumer<RowFormat<T, FixedWidthParserSettings>> onBuild) {
		return new Builder<>(new RowFormat<>(new FixedWidthParserSettings(fixedWidthFields)), onBuild);
	}

	public F parserConfiguration() {
		return parserSettings;
	}

	AbstractParser<?> createParser() {
		setupParserSettings();
		if (parserSettings instanceof CsvParserSettings) {
			return new CsvParser((CsvParserSettings) parserSettings);
		} else if (parserSettings instanceof TsvParserSettings) {
			return new TsvParser((TsvParserSettings) parserSettings);
		} else if (parserSettings instanceof FixedWidthParserSettings) {
			return new FixedWidthParser((FixedWidthParserSettings) parserSettings);
		}
		throw new IllegalStateException("Can't create parser for settings: " + parserSettings);
	}

	private void setupParserSettings() {
		parserSettings.setReadInputOnSeparateThread(false);

		if (parserSettings instanceof CsvParserSettings) {
			CsvParserSettings settings = ((CsvParserSettings) parserSettings);
			settings.detectFormatAutomatically();
		} else {
			parserSettings.setLineSeparatorDetectionEnabled(true);
		}

		parserSettings.setHeaderExtractionEnabled(hasHeaders);

		if (closeDateTime == null) {
			closeDateTime = openDateTime;
		}
		if (open == null) {
			open = close;
		}
		if (high == null) {
			high = close;
		}
		if (low == null) {
			low = close;
		}

		List<T> columns = asList(openDateTime, closeDateTime, open, high, low, close, volume);
		if (close instanceof String) {
			String[] headers = columns.toArray(new String[0]);
			parserSettings.selectFields(headers);
		} else {
			Integer[] indexes = columns.toArray(new Integer[0]);
			parserSettings.selectIndexes(indexes);
		}
		parserSettings.setColumnReorderingEnabled(true);
	}

	private List<T> asList(T... elements) {
		List<T> out = new ArrayList<>(elements.length);
		for (T e : elements) {
			if (e != null) {
				out.add(e);
			}
		}
		return out;
	}

	Candle toCandle(String[] row) {
		try {
			long openTime;
			long closeTime;

			if (dateTimeFormat == null) {
				openTime = Long.parseLong(row[0]);
				closeTime = Long.parseLong(row[1]);
			} else {

				SimpleDateFormat format = dateTimeFormat.get();
				openTime = format.parse(row[0]).getTime();
				closeTime = format.parse(row[1]).getTime();
			}

			return new Candle(
					openTime,
					closeTime,
					Double.parseDouble(row[2]),
					Double.parseDouble(row[3]),
					Double.parseDouble(row[4]),
					Double.parseDouble(row[5]),
					row.length == 7 ? Double.parseDouble(row[6]) : 0
			);
		} catch (Exception e) {
			log.debug("Unable to parse candle data from row {}: {}", Arrays.toString(row), e.getMessage());
			return null;
		}
	}

	public static class Builder<T, F extends CommonParserSettings<?>> implements
			ColumnSelectionType<F>, HeaderConfig<T, F>, DateTimeFormat<T, F>, OpenDateTime<T, F>, CloseDateTime<T, F>,
			OpeningPrice<T, F>, HighestPrice<T, F>, LowestPrice<T, F>, ClosingPrice<T, F>, Volume<T, F>, Build<T, F> {

		private final RowFormat<T, F> rowFormat;
		private final Consumer<RowFormat<T, F>> onBuild;

		private Builder(RowFormat<T, F> rowFormat, Consumer<RowFormat<T, F>> onBuild) {
			this.rowFormat = rowFormat;
			this.onBuild = onBuild;
		}

		@Override
		public HeaderConfig<Integer, F> selectColumnsByIndex() {
			return (Builder<Integer, F>) this;
		}

		@Override
		public DateTimeFormat<String, F> selectColumnsByName() {
			return (Builder<String, F>) this;
		}

		@Override
		public DateTimeFormat<T, F> withHeaderRow() {
			rowFormat.hasHeaders = true;
			return this;
		}

		@Override
		public DateTimeFormat<T, F> noHeaderRow() {
			rowFormat.hasHeaders = false;
			return this;
		}

		@Override
		public OpenDateTime<T, F> dateAndTimePattern(String pattern) {
			rowFormat.dateTimeFormat = pattern == null ? null : ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern));
			return this;
		}

		@Override
		public OpenDateTime<T, F> dateAndTimeInMillis() {
			rowFormat.dateTimeFormat = null;
			return this;
		}

		@Override
		public CloseDateTime<T, F> openDateTime(T column) {
			rowFormat.openDateTime = column;
			return this;
		}

		@Override
		public OpeningPrice<T, F> closeDateTime(T column) {
			rowFormat.closeDateTime = column;
			return this;
		}

		@Override
		public OpeningPrice<T, F> noCloseDateTime() {
			return this;
		}

		@Override
		public HighestPrice<T, F> openingPrice(T column) {
			rowFormat.open = column;
			return this;
		}

		@Override
		public HighestPrice<T, F> noOpeningPrice() {
			return this;
		}

		@Override
		public LowestPrice<T, F> highestPrice(T column) {
			rowFormat.high = column;
			return this;
		}

		@Override
		public LowestPrice<T, F> noHighestPrice() {
			return this;
		}

		@Override
		public ClosingPrice<T, F> lowestPrice(T column) {
			rowFormat.low = column;
			return this;
		}

		@Override
		public ClosingPrice<T, F> noLowestPrice() {
			return this;
		}

		@Override
		public Volume<T, F> closingPrice(T column) {
			rowFormat.close = column;
			return this;
		}

		@Override
		public Build<T, F> volume(T column) {
			rowFormat.volume = column;
			return this;
		}

		@Override
		public Build<T, F> noVolume() {
			return this;
		}

		@Override
		public RowFormat<T, F> build() {
			if (onBuild != null) {
				onBuild.accept(rowFormat);
			}
			return rowFormat;
		}
	}
}

