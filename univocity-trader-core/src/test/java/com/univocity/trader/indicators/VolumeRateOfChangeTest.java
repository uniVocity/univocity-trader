package com.univocity.trader.indicators;




import com.univocity.trader.candles.*;
import org.junit.*;


import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class VolumeRateOfChangeTest {

	private Candle newCandle(int i, double volume) {
		return CandleHelper.newCandle(i, 10.0, 10.0, 10.0, 10.0, volume);
	}

	private double accumulateAndGet(VolumeRateOfChange roc, int i, double volume) {
		roc.accumulate(newCandle(i, volume));
		return roc.getValue();
	}

	@Test
	public void isUsable() {
		VolumeRateOfChange roc = new VolumeRateOfChange(3, minutes(1));

		assertEquals(0, accumulateAndGet(roc, 0, 1000), 0.0001);
		assertEquals(200, accumulateAndGet(roc, 1, 3000), 0.0001);
		assertEquals(250, accumulateAndGet(roc, 2, 3500), 0.0001);
		assertEquals(120, accumulateAndGet(roc, 3, 2200), 0.0001);
		assertEquals(-23.333333333333332, accumulateAndGet(roc, 4, 2300), 0.0001);
		assertEquals(-94.28571428571429, accumulateAndGet(roc, 5, 200), 0.0001);
		assertEquals(22.727272727272727, accumulateAndGet(roc, 6, 2700), 0.0001);
		assertEquals(117.3913043478261, accumulateAndGet(roc, 7, 5000), 0.0001);
		assertEquals(400, accumulateAndGet(roc, 8, 1000), 0.0001);
		assertEquals(-7.407407407407407, accumulateAndGet(roc, 9, 2500), 0.0001);
	}
}