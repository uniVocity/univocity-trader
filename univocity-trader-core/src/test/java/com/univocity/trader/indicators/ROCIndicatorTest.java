package com.univocity.trader.indicators;


import org.junit.*;


import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class ROCIndicatorTest {

	double[] closePrices = new double[]{
			11045.27, 11167.32, 11008.61, 11151.83,
			10926.77, 10868.12, 10520.32, 10380.43,
			10785.14, 10748.26, 10896.91, 10782.95,
			10620.16, 10625.83, 10510.95, 10444.37,
			10068.01, 10193.39, 10066.57, 10043.75};


	private double accumulateAndGet(RateOfChange roc, int i) {
		roc.accumulate(newCandle(i, closePrices[i]));
		return roc.getValue();
	}

	@Test
	public void isUsable() {
		RateOfChange roc = new RateOfChange(12, minutes(1));

		// Incomplete time frame
		assertEquals(0, accumulateAndGet(roc, 0), 0.0001);
		assertEquals(1.105, accumulateAndGet(roc, 1), 0.0001);
		assertEquals(-0.3319, accumulateAndGet(roc, 2), 0.0001);
		assertEquals(0.9648, accumulateAndGet(roc, 3), 0.0001);

		accumulateAndGet(roc, 4);
		accumulateAndGet(roc, 5);
		accumulateAndGet(roc, 6);
		accumulateAndGet(roc, 7);
		accumulateAndGet(roc, 8);
		accumulateAndGet(roc, 9);
		accumulateAndGet(roc, 10);
		accumulateAndGet(roc, 11);

		assertEquals(-3.8488, accumulateAndGet(roc, 12), 0.0001);
		assertEquals(-4.8489, accumulateAndGet(roc, 13), 0.0001);
		assertEquals(-4.5206, accumulateAndGet(roc, 14), 0.0001);
		assertEquals(-6.3439, accumulateAndGet(roc, 15), 0.0001);
		assertEquals(-7.8592, accumulateAndGet(roc, 16), 0.0001);
		assertEquals(-6.2083, accumulateAndGet(roc, 17), 0.0001);
		assertEquals(-4.3131, accumulateAndGet(roc, 18), 0.0001);
		assertEquals(-3.2434, accumulateAndGet(roc, 19), 0.0001);
	}
}