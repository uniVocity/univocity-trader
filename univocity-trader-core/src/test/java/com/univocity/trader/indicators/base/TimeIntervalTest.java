package com.univocity.trader.indicators.base;

import org.junit.*;

import static org.junit.Assert.*;

public class TimeIntervalTest {

	@Test
	public void getFormattedDuration() {
		assertEquals("0 seconds", TimeInterval.getFormattedDuration(0));
		assertEquals("1 second", TimeInterval.getFormattedDuration(1000));
		assertEquals("2 seconds", TimeInterval.getFormattedDuration(2000));
		assertEquals("2 minutes", TimeInterval.getFormattedDuration(2000 * 60));
		assertEquals("2 hours", TimeInterval.getFormattedDuration(2000 * 60 * 60));
		assertEquals("2 hours and 2 minutes", TimeInterval.getFormattedDuration(2000 * 60 * 60 + 2000 * 60));
		assertEquals("2 hours and 2 minutes", TimeInterval.getFormattedDuration(2000 * 60 * 60 + 2000 * 60 + 2000)); // ignore
																														// seconds
	}

	@Test
	public void getFormattedDurationShort() {
		assertEquals("0:00:00", TimeInterval.getFormattedDurationShort(0));
		assertEquals("0:00:01", TimeInterval.getFormattedDurationShort(1000));
		assertEquals("0:00:02", TimeInterval.getFormattedDurationShort(2000));
		assertEquals("0:02:00", TimeInterval.getFormattedDurationShort(2000 * 60));
		assertEquals("2:00:00", TimeInterval.getFormattedDurationShort(2000 * 60 * 60));
		assertEquals("2:02:00", TimeInterval.getFormattedDurationShort(2000 * 60 * 60 + 2000 * 60));
		assertEquals("2:02:02", TimeInterval.getFormattedDurationShort(2000 * 60 * 60 + 2000 * 60 + 2000));
		assertEquals("20:02:02", TimeInterval.getFormattedDurationShort(20000 * 60 * 60 + 2000 * 60 + 2000));
	}
}