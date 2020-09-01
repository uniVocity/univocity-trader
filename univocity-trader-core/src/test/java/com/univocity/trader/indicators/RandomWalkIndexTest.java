package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class RandomWalkIndexTest {

	@Test
	public void rwi() {

		int i = 0;

		RandomWalkIndex indicator = new RandomWalkIndex(20, TimeInterval.MINUTE);

		indicator.accumulate(CandleHelper.newCandle(i++, 229.35, 224.8, 229.7, 219.5, 121888889));
		indicator.accumulate(CandleHelper.newCandle(i++, 225.7, 219.4, 228, 217.5, 150522222));
		indicator.accumulate(CandleHelper.newCandle(i++, 220.15, 237, 237.5, 216.6, 152477778));
		indicator.accumulate(CandleHelper.newCandle(i++, 237.5, 240.2, 243.2, 234, 168800000));
		indicator.accumulate(CandleHelper.newCandle(i++, 240.25, 242.5, 243.5, 232, 149655556));
		indicator.accumulate(CandleHelper.newCandle(i++, 242.55, 247.3, 257.55, 240, 140211111));
		indicator.accumulate(CandleHelper.newCandle(i++, 248, 250.6, 253.3, 240.5, 133288889));
		indicator.accumulate(CandleHelper.newCandle(i++, 251, 255.7, 258.8, 248.5, 107888889));
		indicator.accumulate(CandleHelper.newCandle(i++, 255.85, 243.2, 257, 242, 140155556));
		indicator.accumulate(CandleHelper.newCandle(i++, 243.65, 245.9, 246.9, 235, 160577777));
		indicator.accumulate(CandleHelper.newCandle(i++, 246.8, 248, 250.1, 242, 158533333));
		indicator.accumulate(CandleHelper.newCandle(i++, 248.1, 246.4, 249, 243.5, 178744445));
		indicator.accumulate(CandleHelper.newCandle(i++, 247.3, 243.8, 252, 241, 171966667));
		indicator.accumulate(CandleHelper.newCandle(i++, 244, 244.3, 246, 241.3, 192988890));
		indicator.accumulate(CandleHelper.newCandle(i++, 245.25, 235.6, 245.25, 234.9, 132011112));
		indicator.accumulate(CandleHelper.newCandle(i++, 236.15, 239, 244.3, 231.8, 163111111));
		indicator.accumulate(CandleHelper.newCandle(i++, 239.65, 248.5, 248.8, 237.8, 172122223));
		indicator.accumulate(CandleHelper.newCandle(i++, 248.55, 250.3, 251.5, 246.2, 166100000));
		indicator.accumulate(CandleHelper.newCandle(i++, 250.65, 249.8, 255.3, 246.1, 155677778)); //20 candles

		indicator.accumulate(CandleHelper.newCandle(i++, 250.1, 274.4, 274.9, 250.1, 149033332));
		assertEquals(0.2241, indicator.getRwiLow(), 0.0001);
		assertEquals(1.5273, indicator.getRwiHigh(), 0.0001);
        assertEquals(0.7440, indicator.getOscillator(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 275.4, 275.3, 281.5, 267.5, 155400001));
		assertEquals(0.2676, indicator.getRwiLow(), 0.0001);
		assertEquals(1.5789, indicator.getRwiHigh(), 0.0001);
        assertEquals(0.7101, indicator.getOscillator(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i++, 275.85, 276.9, 282, 269.5, 123500000));
		assertEquals(0.6403, indicator.getRwiLow(), 0.0001);
		assertEquals(1.4823, indicator.getRwiHigh(), 0.0001);
        assertEquals(0.3966, indicator.getOscillator(), 0.0001);

	}

}
