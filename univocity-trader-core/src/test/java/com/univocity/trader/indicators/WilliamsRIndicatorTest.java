package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class WilliamsRIndicatorTest {

	@Test
	public void test() {
		int i = 0;

		WilliamsRIndicator indicator = new WilliamsRIndicator(5, minutes(1));

		indicator.accumulate(CandleHelper.newCandle(i++, 44.98, 45.05, 45.17, 44.96));
		indicator.accumulate(CandleHelper.newCandle(i++, 45.05, 45.10, 45.15, 44.99));
		indicator.accumulate(CandleHelper.newCandle(i++, 45.11, 45.19, 45.32, 45.11));
		indicator.accumulate(CandleHelper.newCandle(i++, 45.19, 45.14, 45.25, 45.04));
		indicator.accumulate(CandleHelper.newCandle(i++, 45.12, 45.15, 45.20, 45.10));
		assertEquals(-47.2222, indicator.getValue(), 0.0001);
		indicator.accumulate(CandleHelper.newCandle(i++, 45.15, 45.14, 45.20, 45.10));
		assertEquals(-54.5454, indicator.getValue(), 0.0001);
		indicator.accumulate(CandleHelper.newCandle(i++, 45.13, 45.10, 45.16, 45.07));
		assertEquals(-78.5714, indicator.getValue(), 0.0001);
		indicator.accumulate(CandleHelper.newCandle(i++, 45.12, 45.15, 45.22, 45.10));
		assertEquals(-47.6190, indicator.getValue(), 0.0001);
		indicator.accumulate(CandleHelper.newCandle(i++, 45.15, 45.22, 45.27, 45.14));
		assertEquals(-25d, indicator.getValue(), 0.0001);
		indicator.accumulate(CandleHelper.newCandle(i++, 45.24, 45.43, 45.45, 45.20));
		assertEquals(-5.2632, indicator.getValue(), 0.0001);
		indicator.accumulate(CandleHelper.newCandle(i++, 45.43, 45.44, 45.50, 45.39));
		assertEquals(-13.9535, indicator.getValue(), 0.0001);



	}

}