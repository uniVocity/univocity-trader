package com.univocity.trader.indicators;



import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;


public class DonchianChannelTest {

	/**
	 * Test the donchian channel with a length of 1
	 */
	@Test
	public void testDonchianChannel1() {
		DonchianChannel dcu = new DonchianChannel(1, minutes(1));

		for (int i = 0; i < 10; i++) {
			dcu.accumulate(CandleHelper.newCandle(i, i));
			assertEquals(i, dcu.getUpperBand(), 0.00000001);
			assertEquals(i, dcu.getLowerBand(), 0.00000001);
		}
	}

	private void accumulateAndTest(DonchianChannel dcu, int i, double value, int expectedUpper, int expectedLower) {
		dcu.accumulate(CandleHelper.newCandle(i, value));
		assertEquals("Upper band", expectedUpper, dcu.getUpperBand(), 0.00000001);
		assertEquals("Lower band", expectedLower, dcu.getLowerBand(), 0.00000001);
	}

	/**
	 * Test the donchian channel with a length of 3
	 */
	@Test
	public void testDonchianChannel3() {
		DonchianChannel dcu = new DonchianChannel(3, minutes(1));

		accumulateAndTest(dcu, 1, 1, 1, 1);
		accumulateAndTest(dcu, 2, 2, 2, 1);
		accumulateAndTest(dcu, 3, 3, 3, 1);
		accumulateAndTest(dcu, 4, 4, 4, 2);
		accumulateAndTest(dcu, 5, 3, 4, 3);
		accumulateAndTest(dcu, 6, 4, 4, 3);
		accumulateAndTest(dcu, 7, 5, 5, 3);
		accumulateAndTest(dcu, 8, 4, 5, 4);
		accumulateAndTest(dcu, 9, 3, 5, 3);
		accumulateAndTest(dcu, 10, 3, 4, 3);
		accumulateAndTest(dcu, 11, 4, 4, 3);
		accumulateAndTest(dcu, 12, 3, 4, 3);
		accumulateAndTest(dcu, 13, 2, 4, 2);
		accumulateAndTest(dcu, 14, 2, 3, 2);
	}

	/**
	 * Test the donchian channel with a length of 20
	 */
	@Test
	public void testUpperDonchianChannel20() {
		DonchianChannel dcu = new DonchianChannel(20, minutes(1));

		accumulateAndTest(dcu, 1, 1, 1, 1);
		accumulateAndTest(dcu, 2, 2, 2, 1);
		accumulateAndTest(dcu, 3, 3, 3, 1);
		accumulateAndTest(dcu, 4, 4, 4, 1);
		accumulateAndTest(dcu, 5, 3, 4, 1);
		accumulateAndTest(dcu, 6, 4, 4, 1);
		accumulateAndTest(dcu, 7, 5, 5, 1);
		accumulateAndTest(dcu, 8, 4, 5, 1);
		accumulateAndTest(dcu, 9, 3, 5, 1);
		accumulateAndTest(dcu, 10, 3, 5, 1);
		accumulateAndTest(dcu, 11, 4, 5, 1);
		accumulateAndTest(dcu, 12, 3, 5, 1);
		accumulateAndTest(dcu, 13, 2, 5, 1);
	}

	/**
	 * Test the 2 min donchian channel with a length of 3
	 */
	@Test
	public void testDonchianChannel3_2() {
		DonchianChannel dcu = new DonchianChannel(3, minutes(2));
		dcu.recalculateEveryTick(true);

		accumulateAndTest(dcu, 1, 1, 1, 1);
		accumulateAndTest(dcu, 2, 2, 2, 1);

		accumulateAndTest(dcu, 3, 3, 3, 1);
		accumulateAndTest(dcu, 4, 4, 4, 1);

		accumulateAndTest(dcu, 5, 3, 4, 1);
		accumulateAndTest(dcu, 6, 4, 4, 1);

		accumulateAndTest(dcu, 7, 5, 5, 3);
		accumulateAndTest(dcu, 8, 4, 5, 3);

		accumulateAndTest(dcu, 9, 3, 5, 3);
		accumulateAndTest(dcu, 10, 3, 5, 3);

		accumulateAndTest(dcu, 11, 4, 5, 3);
		accumulateAndTest(dcu, 12, 3, 5, 3);

		accumulateAndTest(dcu, 13, 2, 4, 2);
		accumulateAndTest(dcu, 14, 1, 4, 1);
	}

}