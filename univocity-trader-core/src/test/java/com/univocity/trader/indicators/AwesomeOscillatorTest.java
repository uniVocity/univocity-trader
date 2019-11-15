package com.univocity.trader.indicators;


import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class AwesomeOscillatorTest {


	private static final double[][] values1 = new double[][]{
			{16, 8},
			{12, 6},
			{18, 14},
			{10, 6},
			{8, 4},
	};
	private static final double[][] values2 = new double[][]{
			{16, 10},
			{13, 8}, //16, 8

			{11, 8}, //12, 6
			{12, 6},

			{18, 15}, //18, 14
			{16, 14},

			{6, 6}, //10, 6
			{10, 9},

			{5, 4}, //8, 4
			{8, 6},
	};

	@Test
	public void calculateWithSma2AndSma3() {
		AwesomeOscillator i = new AwesomeOscillator(2, 3, minutes(1));

		assertEquals(0.0, accumulate(i, newCandle(1, 10.0, 10.0, 16, 8)), 0.0001);
		assertEquals(0.0, accumulate(i, newCandle(2, 10.0, 10.0, 12, 6)), 0.0001);

		assertEquals(1.0 / 6.0, accumulate(i, newCandle(3, 10.0, 10.0, 18, 14)), 0.0001);
		assertEquals(1.0, accumulate(i, newCandle(4, 10.0, 10.0, 10, 6)), 0.0001);
		assertEquals(-3.0, accumulate(i, newCandle(5, 10.0, 10.0, 8, 4)), 0.0001);
	}
//
//	@Test
//	public void calculateWithSma2AndSma3Interval2() {
//		AwesomeOscillator i = new AwesomeOscillator(2, 3, 2);
//
//		accumulate2(0, i);
//		accumulate2(1, i);
//		assertEquals(0.0, i.getValue(), 0.0001);
//		assertEquals(0.0, i.bars.values[0], 0.0001);
//		assertEquals(0.0, i.bars.values[1], 0.0001);
//
//		accumulate2(2, i);
//		accumulate2(3, i);
//		assertEquals(0.0, i.getValue(), 0.0001);
//		assertEquals(0.0, i.bars.values[0], 0.0001);
//		assertEquals(0.0, i.bars.values[1], 0.0001);
//
//		accumulate2(4, i);
//		accumulate2(5, i);
//		assertEquals(1.0 / 6.0, i.getValue(), 0.0001);
//		assertEquals(0.0, i.bars.values[0], 0.0001);
//		assertEquals(0.0, i.bars.values[1], 0.0001);
//		assertEquals(1.0 / 6.0, i.bars.values[2], 0.0001);
//
//		accumulate2(6, i);
//		accumulate2(7, i);
//		assertEquals(1.0, i.getValue(), 0.0001);
//		assertEquals(1.0, i.bars.values[0], 0.0001);
//		assertEquals(0.0, i.bars.values[1], 0.0001);
//		assertEquals(1.0 / 6.0, i.bars.values[2], 0.0001);
//
//		accumulate2(8, i);
//		accumulate2(9, i);
//		assertEquals(-3.0, i.getValue(), 0.0001);
//		assertEquals(1.0, i.bars.values[0], 0.0001);
//		assertEquals(-3.0, i.bars.values[1], 0.0001);
//		assertEquals(1.0 / 6.0, i.bars.values[2], 0.0001);
//
//	}
//
//	@Test
//	public void withSma1AndSma2() {
//		AwesomeOscillator i = new AwesomeOscillator(1, 2, 1);
//
//		accumulate(0, i);
//		assertEquals(0.0, i.getValue(), 0.0001);
//		assertEquals(0.0, i.bars.values[0], 0.0001); //not usable
//
//		accumulate(1, i);
//		assertEquals(-1.5, i.getValue(), 0.0001);
//		assertEquals(0.0, i.bars.values[0], 0.0001); //not usable
//		assertEquals(-1.5, i.bars.values[1], 0.0001);
//
//		accumulate(2, i);
//		assertEquals(3.5, i.getValue(), 0.0001);
//		assertEquals(0.0, i.bars.values[0], 0.0001); //not usable
//		assertEquals(-1.5, i.bars.values[1], 0.0001);
//		assertEquals(3.5, i.bars.values[2], 0.0001);
//
//		accumulate(3, i);
//		assertEquals(-4.0, i.getValue(), 0.0001);
//		assertEquals(-4.0, i.bars.values[0], 0.0001); //not usable
//		assertEquals(-1.5, i.bars.values[1], 0.0001);
//		assertEquals(3.5, i.bars.values[2], 0.0001);
//
//		accumulate(4, i);
//		assertEquals(-1.0, i.getValue(), 0.0001);
//		assertEquals(-4.0, i.bars.values[0], 0.0001); //not usable
//		assertEquals(-1.0, i.bars.values[1], 0.0001);
//		assertEquals(3.5, i.bars.values[2], 0.0001);
//	}

}