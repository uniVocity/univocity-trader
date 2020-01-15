package com.univocity.trader.config;

import org.junit.*;

import static org.junit.Assert.*;

public class UtilsTest {

	@Test
	public void countDecimals() {
		assertEquals(1, Utils.countDecimals(Double.parseDouble("0.1")));
		assertEquals(2, Utils.countDecimals(Double.parseDouble("0.01")));
		assertEquals(5, Utils.countDecimals(Double.parseDouble("0.00001")));
		assertEquals(5, Utils.countDecimals(5.0E-5));
	}
}