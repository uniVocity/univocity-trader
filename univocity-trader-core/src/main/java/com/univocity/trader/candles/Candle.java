package com.univocity.trader.candles;

import java.text.*;
import java.time.*;
import java.time.format.*;

public class Candle implements Comparable<Candle> {
	private static final ThreadLocal<DateTimeFormatter> DATE_FORMAT = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("MMM dd HH:mm"));
	private static final ThreadLocal<DateTimeFormatter> DATE_YEAR_FORMAT = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm"));

	public static final ThreadLocal<DecimalFormat> PRICE_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00000000"));
	public static final ThreadLocal<DecimalFormat> CHANGE_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00%"));
	public static final ThreadLocal<DecimalFormat> VOLUME_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,###"));

	public final long openTime;
	public long closeTime;
	public final double open;
	public double high;
	public double low;
	public double close;
	public double volume;
	public final boolean merged;

	public Candle(long openTime, long closeTime, double open, double high, double low, double close, double volume) {
		this(openTime, closeTime, open, high, low, close, volume, false);
	}

	private Candle(long openTime, long closeTime, double open, double high, double low, double close, double volume, boolean merged) {
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.merged = merged;
	}

	public String getFormattedCloseTime(String pattern) {
		return getFormattedCloseTime(pattern, ZoneId.systemDefault());
	}

	public String getFormattedCloseTime(String pattern, ZoneId timezone) {
		return getFormattedDateTime(closeTime, DateTimeFormatter.ofPattern(pattern), timezone);
	}

	public String getFormattedCloseTime() {
		return getFormattedCloseTime(ZoneId.systemDefault());
	}

	public String getFormattedCloseTime(ZoneId timezone) {
		return getFormattedDateTime(closeTime, timezone);
	}

	public String getFormattedCloseTimeWithYear() {
		return getFormattedDateTimeWithYear(closeTime);
	}

	public static String getFormattedDateTime(long timeInMs, ZoneId timezone) {
		return getFormattedDateTime(timeInMs, DATE_FORMAT.get(), timezone);
	}

	public static String getFormattedDateTimeWithYear(long timeInMs) {
		return getFormattedDateTimeWithYear(timeInMs, ZoneId.systemDefault());
	}

	public static String getFormattedDateTimeWithYear(long timeInMs, ZoneId timezone) {
		return getFormattedDateTime(timeInMs, DATE_YEAR_FORMAT.get(), timezone);
	}

	public static String getFormattedDateTime(long timeInMs, DateTimeFormatter formatter, ZoneId timezone) {
		LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMs), timezone);
		return dateTime.format(formatter);
	}

	@Override
	public String toString() {
		return getFormattedCloseTime(ZoneId.systemDefault()) + " | O(" + formatPrice(open) + "), C(" + formatPrice(close) + "),  H(" + formatPrice(high) + "), L(" + formatPrice(low) + "), V(" + formatVolume(volume) + ")";
	}

	public static String formatPrice(double v) {
		return PRICE_FORMAT.get().format(v);
	}

	public static String formatVolume(double v) {
		return VOLUME_FORMAT.get().format(v);
	}

	public double getChange() {
		return (close / open) - 1.0;
	}

	public String getFormattedChange() {
		return CHANGE_FORMAT.get().format(getChange());
	}

	public Candle merge(Candle o) {
		if (o == this) {
			return this;
		}

		if(this.merged){
			this.closeTime = o.closeTime;
			this.high = Math.max(this.high, o.high);
			this.low = Math.min(this.low, o.low);
			this.close = o.close;
			this.volume = this.volume + o.volume;
			return this;
		}

		return new Candle(
				/* openTime  */ this.openTime,
				/* closeTime */ o.closeTime,
				/* open      */ this.open,
				/* high      */ Math.max(this.high, o.high),
				/* low       */ Math.min(this.low, o.low),
				/* close     */ o.close,
				/* volume    */this.volume + o.volume,
				/* merged?   */ true
		);
	}

	@Override
	public int compareTo(Candle o) {
		if (o == this) {
			return 0;
		}
		if (this.closeTime == o.closeTime) {
			if (this.openTime == o.openTime) {
				return 0;
			} else {
				return this.openTime < o.openTime ? -1 : 1;
			}
		}
		return this.closeTime < o.closeTime ? -1 : 1;
	}


	public boolean isClosePositive() {
		return close > open;
	}

	public boolean isGreen() {
		return this.open < this.close;
	}

	public boolean isRed() {
		return this.open >= this.close;
	}
}
