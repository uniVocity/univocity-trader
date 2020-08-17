package com.univocity.trader.indicators;

import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static org.junit.Assert.*;

public class AroonUpTest {

	@Test
	public void aroon() {

		int i = 0;

		AroonUp indicator = new AroonUp(5, TimeInterval.MINUTE);
		accumulateAndTest(100, indicator, i++, 169.87);
		accumulateAndTest(80.0, indicator, i++, 169.36);
		accumulateAndTest(60.0, indicator, i++, 169.29);
		accumulateAndTest(40.0, indicator, i++, 168.38);
		accumulateAndTest(20.0, indicator, i++,  167.7);
		accumulateAndTest(0.0, indicator, i++, 168.43);
		accumulateAndTest(100.0, indicator, i++, 170.18);
		accumulateAndTest(100.0, indicator, i++, 172.15);
		accumulateAndTest(100.0, indicator, i++, 172.92);
		accumulateAndTest(80.0, indicator, i++, 172.39);
		accumulateAndTest(60.0, indicator, i++, 172.48);
		accumulateAndTest(100.0, indicator, i++, 173.31);
		accumulateAndTest(100.0, indicator, i++, 173.49);
		accumulateAndTest(100.0, indicator, i++, 173.89);
		accumulateAndTest(100.0, indicator, i++, 174.17);
		accumulateAndTest(80.0, indicator, i++, 173.17);
		accumulateAndTest(60.0, indicator, i++, 172.28);
		accumulateAndTest(40.0, indicator, i++, 172.34);
		accumulateAndTest(20.0, indicator, i++, 172.07);
		accumulateAndTest(0.0, indicator, i++, 172.56);
	}

	private void accumulateAndTest(double expected, AroonUp indicator, int i, double valueToAccumulate) {
		indicator.accumulate(newCandle(i, valueToAccumulate));
		double value = indicator.getValue();
		assertEquals(expected, value, 0.00001);
	}
}
