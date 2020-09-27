package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class StochasticRSITest {

	private static final double[] prices = new double[]{
			50.45, 50.30, 50.20, 50.15, 50.05,
			50.06, 50.10, 50.08, 50.03, 50.07,
			50.01, 50.14, 50.22, 50.43, 50.50,
			50.56, 50.52, 50.70, 50.55, 50.62,
			50.90, 50.82, 50.86, 51.20, 51.30,
			51.10};

	private Candle newCandle(int i) {
		return CandleHelper.newCandle(i, prices[i]);
	}

	@Test
	public void isUsable() {
		StochasticRSI b = new StochasticRSI(14, minutes(1));

		int i = 0;
		for (; i < 16; i++) {
			b.accumulate(newCandle(i));
		}

		assertEquals(100.0, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(97.10911690154317, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(100.0, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(89.9358415444569, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(96.68901901082249, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(100.0, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(94.74635149460414, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(97.35719258439879, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(100.0, b.getValue(), 0.001);
		b.accumulate(newCandle(i));
		assertEquals(100.0, b.getValue(), 0.001);

	}
}
