package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class StandardDeviationTest {

	double values[] = {1, 2, 3, 4, 3, 4, 5, 4, 3, 0, 9};

	@Test
	public void standardDeviationUsingBarCount4UsingClosePrice() {

		int i = 0;

		StandardDeviation indicator = new StandardDeviation(4, TimeInterval.MINUTE);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(0.25), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(2.0 / 3), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(1.25), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(0.25), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(0.5), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(3.5), indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(Math.sqrt(10.5), indicator.getValue(), 0.0001);

	}

	@Test
	public void standardDeviationShouldBeZeroWhenBarCountIs1() {

		int i = 0;

		StandardDeviation indicator = new StandardDeviation(1, TimeInterval.MINUTE);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(0, indicator.getValue(), 0.0001);

	}

}
