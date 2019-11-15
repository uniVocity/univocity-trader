package com.univocity.trader.indicators;



import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class StochasticOscillatorKTest {

	@Test
	public void testValues() {

		StochasticOscillatorK ind = new StochasticOscillatorK(minutes(1));

		double[][] values = new double[][]{
				{44.98, 119.13, 119.50, 116.00},
				{45.05, 116.75, 119.94, 116.00},
				{45.11, 113.50, 118.44, 111.63},
				{45.19, 111.56, 114.19, 110.06},
				{45.12, 112.25, 112.81, 109.63},
				{45.15, 110.00, 113.44, 109.13},
				{45.13, 113.50, 115.81, 110.38},
				{45.12, 117.13, 117.50, 114.06},
				{45.15, 115.63, 118.44, 114.81},
				{45.24, 114.13, 116.88, 113.13},
				{45.43, 118.81, 119.00, 116.19},
				{45.43, 117.38, 119.75, 117.00},
				{45.58, 119.13, 119.13, 116.88},
				{45.58, 115.38, 119.44, 114.56},
		};

		int i = 0;

		for (double[] v : values) {
			Candle c = newCandle(i, v[0], v[1], v[2], v[3]);
			ind.accumulate(c);

			if (i == 0) {
				assertEquals(313 / 3.5, ind.getValue(), 0.001);
			} else if (i == 12) {
				assertEquals(1000 / 10.81, ind.getValue(), 0.001);
			} else if (i == 13) {
				assertEquals(57.8168, ind.getValue(), 0.001);
			}

			i++;
		}
	}
}

