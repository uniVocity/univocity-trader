package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static org.junit.Assert.*;

public class AroonDownTest {

	@Test
	public void aroon() {

		int i = 0;

		AroonDown indicator = new AroonDown(5, TimeInterval.MINUTE);
		accumulateAndTest(100.0, indicator, i++, 167.15);
		accumulateAndTest(80.0, indicator, i++, 168.2);
		accumulateAndTest(100.0, indicator, i++, 166.41);
		accumulateAndTest(100.0, indicator, i++, 166.18);
		accumulateAndTest(80.0, indicator, i++, 166.33);
		accumulateAndTest(100.0, indicator, i++, 165);
		accumulateAndTest(80.0, indicator, i++, 167.63);
		accumulateAndTest(60.0, indicator, i++, 170.06);
		accumulateAndTest(40.0, indicator, i++, 171.31);
		accumulateAndTest(20.0, indicator, i++, 169.55);
		accumulateAndTest(0.0, indicator, i++, 169.57);
		accumulateAndTest(0.0, indicator, i++, 170.27);
		accumulateAndTest(40.0, indicator, i++, 170.8);
		accumulateAndTest(20.0, indicator, i++, 172.2);
		accumulateAndTest(0.0, indicator, i++, 175);
		accumulateAndTest(0.0, indicator, i++, 172.06);
		accumulateAndTest(0.0, indicator, i++, 170.5);
		accumulateAndTest(100.0, indicator, i++, 170.26);
		accumulateAndTest(100.0, indicator, i++, 169.34);
		accumulateAndTest(80.0, indicator, i++, 170.36);
	}

	private void accumulateAndTest(double expected, AroonDown indicator, int i, double valueToAccumulate) {
		indicator.accumulate(newCandle(i, valueToAccumulate));
		double value = indicator.getValue();
		assertEquals(expected, value, 0.00001);
	}
}
