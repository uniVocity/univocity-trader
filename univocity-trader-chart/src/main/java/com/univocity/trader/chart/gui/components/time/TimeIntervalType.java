package com.univocity.trader.chart.gui.components.time;

import com.univocity.trader.indicators.base.*;

import java.util.*;

public enum TimeIntervalType {

	SECOND(Calendar.SECOND), MINUTE(Calendar.MINUTE), HOUR(Calendar.HOUR_OF_DAY), DAY(Calendar.DAY_OF_MONTH), WEEK(Calendar.WEEK_OF_YEAR), MONTH(Calendar.MONTH),
	;

	private final int calendarField;

	TimeIntervalType(int calendarField) {
		this.calendarField = calendarField;
	}

	public int readField(Calendar date) {
		return date.get(calendarField);
	}

	public int getCalendarField() {
		return calendarField;
	}

	public String toString() {
		String str = super.toString().toLowerCase() + "s";
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	public long convertMillisToUnits(long timeInMillis) {
		long units = timeInMillis;
		switch (this) {
			case WEEK:
				units /= 7;
			case DAY:
				units /= 24;
			case HOUR:
				units /= 60;
			case MINUTE:
				units /= 60;
			case SECOND:
				units /= 1000;
				break;
			default:
				throw new UnsupportedOperationException("Can't convert millis to " + this.toString());

		}
		return units;
	}

	public int convertTo(TimeIntervalType newUnit) {
		int units = 1;
		if (this.equals(newUnit)) {
			return units;
		}
		if (this.compareTo(newUnit) < 0) {
			throw new IllegalArgumentException("Can't convert a unit to a bigger unit: " + this.toString() + " => " + newUnit.toString());
		}

		switch (newUnit) {
			case SECOND:
				if (this.compareTo(SECOND) > 0) {
					units *= MINUTE.getMaxNumberOfUnits();
				}
			case MINUTE:
				if (this.compareTo(MINUTE) > 0) {
					units *= HOUR.getMaxNumberOfUnits();
				}
			case HOUR:
				if (this.compareTo(HOUR) > 0) {
					units *= DAY.getMaxNumberOfUnits();
				}
			case DAY:
				if (this.compareTo(DAY) > 0) {
					units *= WEEK.getMaxNumberOfUnits();
				}
			case WEEK:
				if (this.compareTo(WEEK) > 0) {
					units *= MONTH.getMaxNumberOfUnits();
				}
		}
		return units;
	}

	public int getMaxNumberOfUnits() {
		switch (this) {
			case MONTH:
				return 31;
			case WEEK:
				return 7;
			case DAY:
				return 24;
			case HOUR:
			case MINUTE:
			case SECOND:
				return 60;
		}
		return -1;
	}

	public void clear(Calendar calendar) {
		switch (this) {
			case WEEK:
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			case MONTH:
				if (this != WEEK) {
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					calendar.add(Calendar.MONTH, 1);
					calendar.add(Calendar.DAY_OF_MONTH, -1);
				}
			case DAY:
				calendar.set(Calendar.HOUR_OF_DAY, 0);
			case HOUR:
				calendar.clear(Calendar.MINUTE);
			case MINUTE:
				calendar.clear(Calendar.SECOND);
			case SECOND:
				calendar.clear(Calendar.MILLISECOND);
		}
	}

	public TimeInterval toTimeInterval(int units) {
		switch (this) {
			case WEEK:
				return TimeInterval.weeks(units);
			case DAY:
				return TimeInterval.days(units);
			case HOUR:
				return TimeInterval.hours(units);
			case MINUTE:
				return TimeInterval.minutes(units);
			case SECOND:
				return TimeInterval.seconds(units);
			default:
				throw new UnsupportedOperationException("Can't convert units to " + this.toString());
		}
	}
}
