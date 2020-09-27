package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class RsiIndicatorTest {

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
		RSI b = new RSI(14, minutes(1));

		int i = 0;
		for (; i < 16; i++) {
			b.accumulate(newCandle(i));
		}

		assertEquals(35.777901460618764, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(34.7436041543112, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(42.76239976930923, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(38.51503293636092, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(41.438263768223955, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(51.39315031803166, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(48.83866844885807, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(50.172261873525684, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(59.771183961971296, b.getValue(), 0.001);
		b.accumulate(newCandle(i++));
		assertEquals(62.08469518910162, b.getValue(), 0.001);
		b.accumulate(newCandle(i));
		assertEquals(55.24212283343575, b.getValue(), 0.001);

	}
}
