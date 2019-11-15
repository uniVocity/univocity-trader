package com.univocity.trader.indicators;




import com.univocity.trader.candles.*;
import com.univocity.trader.strategy.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;

import static org.junit.Assert.*;

public class ChandelierExitTest {

	@Test
	public void testExitLong() {
		double[] expected = new double[]{44.9853, 45.0162, 44.9590, 44.9852, 45.1221, 45.1937, 45.2549, 45.2459, 45.0187, 44.7890};
		ChandelierExitLong ind = new ChandelierExitLong(5, minutes(1), 2.0);
		testValues(ind, expected);
	}

	@Test
	public void testExitShort() {
		double[] expected = new double[]{45.3246,45.3437,45.3309,45.3547,45.3978,45.3762,45.4450,45.5040,45.3912,44.9909};
		ChandelierExitShort ind = new ChandelierExitShort(5, minutes(1), 2.0);
		testValues(ind, expected);
	}

	private void testValues(Indicator ind, double[] expected) {
		double[][] values = new double[][]{
				{45.05, 45.17, 44.96},
				{45.10, 45.15, 44.99},
				{45.19, 45.32, 45.11},
				{45.14, 45.25, 45.04},
				{45.15, 45.20, 45.10},
				{45.14, 45.20, 45.10},
				{45.10, 45.16, 45.07},
				{45.15, 45.22, 45.10},
				{45.22, 45.27, 45.14},
				{45.43, 45.45, 45.20},
				{45.44, 45.50, 45.39},
				{45.55, 45.60, 45.35},
				{45.55, 45.61, 45.39},
				{45.01, 45.55, 44.80},
				{44.23, 45.04, 44.17},
		};

		for (int i = 0; i < 15; i++) {
			double result = accumulate(ind, values, i);
			if (i >= 5) {
				if (expected[i - 5] != result) {
					assertEquals("Error at " + i, expected[i - 5], result, 0.001);
				}
			}
		}
	}

	private double accumulate(Indicator ind, double[][] vals, int i) {
		double[] v = vals[i];
		Candle c = newCandle(i, v[0], v[0], v[1], v[2]);
		ind.accumulate(c);

		return ind.getValue();
	}
}