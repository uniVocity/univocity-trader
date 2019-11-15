package com.univocity.trader.indicators;



import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

import static org.junit.Assert.*;

public class InstantaneousTrendlineTest {
	private Candle newCandle(int i, int time) {
		double[] prices = new double[]{i + 1, i + 1.5, i + 0.5};
		return CandleHelper.newCandle(time, prices[0], prices[0], prices[1], prices[2]);
	}

	private String accumulateTestTrend(InstantaneousTrendline trend, int i, int time) {
		Candle c = newCandle(i, time);
		trend.accumulate(c);
		String out = i + "]\t" + c.close + "\t" + c.high + "\t" + c.low+ "\t" + trend.getTrendLine() + "\t" + trend.getZl();
		// System.out.println(out);
		return out;
	}

	@Test
	public void testTrendline() {
		InstantaneousTrendline trend = new InstantaneousTrendline(minutes(1));
		int time = 0;
		String out = "";
		for (int j = 0; j < 100; j++) {
			out = accumulateTestTrend(trend, j, time++);
		}
		assertEquals("99]\t100.0\t100.5\t99.5\t100.0\t97.43939391637966", out);

		for (int j = 100; j >= 0; j--) {
			out = accumulateTestTrend(trend, j, time++);
		}
		assertEquals("0]\t1.0\t1.5\t0.5\t1.0\t3.5606060512907622", out);
	}
}
