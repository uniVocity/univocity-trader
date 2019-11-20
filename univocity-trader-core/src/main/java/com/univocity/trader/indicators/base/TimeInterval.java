package com.univocity.trader.indicators.base;

import java.util.concurrent.*;

public class TimeInterval {

	public static final TimeInterval MINUTE = minutes(1);
	public static final TimeInterval HOUR = hours(1);

	public final long duration;
	public final TimeUnit unit;
	public final long ms;
	private String unitStr;

	private TimeInterval(long duration, TimeUnit unit) {
		this.duration = duration;
		this.unit = unit;
		this.ms = unit.toMillis(duration);
		unitStr = getUnitStr(unit);

	}

	private static String getUnitStr(TimeUnit unit) {
		switch (unit) {
			case DAYS:
				return "d";
			case HOURS:
				return "h";
			case MINUTES:
				return "m";
			case SECONDS:
				return "s";
			case MILLISECONDS:
				return "ms";
		}
		return "";
	}

	public static TimeInterval millis(long duration) {
		return new TimeInterval(duration, TimeUnit.MILLISECONDS);
	}

	public static TimeInterval seconds(long duration) {
		return new TimeInterval(duration, TimeUnit.SECONDS);
	}

	public static TimeInterval minutes(long duration) {
		return new TimeInterval(duration, TimeUnit.MINUTES);
	}

	public static TimeInterval hours(long duration) {
		return new TimeInterval(duration, TimeUnit.HOURS);
	}

	public static TimeInterval days(long duration) {
		return new TimeInterval(duration, TimeUnit.DAYS);
	}

	public static TimeInterval weeks(long duration) {
		return new TimeInterval(duration * 7, TimeUnit.DAYS);
	}

	public static TimeInterval months(long duration) {
		return new TimeInterval(duration * 30, TimeUnit.DAYS);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimeInterval that = (TimeInterval) o;

		return ms == that.ms;
	}

	@Override
	public int hashCode() {
		return (int) (ms ^ (ms >>> 32));
	}

	@Override
	public String toString() {
		return duration + unitStr;
	}

	public static String getFormattedDuration(TimeInterval interval) {
		return getFormattedDuration(interval.ms);
	}

	public static String getFormattedDuration(long ms) {
		long seconds = ms / 1000;
		long minutes = ms / MINUTE.ms;
		long hours = ms / HOUR.ms;
		if (hours > 0) {
			minutes -= hours * 60;
			if (minutes == 0) {
				return pluralize("hour", hours);
			} else {
				return pluralize("hour", hours) + " and " + pluralize("minute", minutes);
			}
		}
		if(minutes > 0){
			seconds -= minutes * 60;
			if (seconds == 0) {
				return pluralize("minute", minutes);
			} else {
				return pluralize("minute", minutes) + " and " + pluralize("second", seconds);
			}
		}
		return pluralize("second", seconds);
	}

	private static String pluralize(String word, long len) {
		if (len != 1) {
			return len + " " + word + 's';
		}
		return len + " " + word;
	}
}
