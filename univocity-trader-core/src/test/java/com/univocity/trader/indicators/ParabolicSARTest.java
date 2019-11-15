package com.univocity.trader.indicators;




import com.univocity.trader.candles.*;
import org.junit.*;


import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class ParabolicSARTest {

	private static final double[][] prices = new double[][]{
			{75.1, 74.06, 75.11},
			{75.9, 76.030000, 74.640000},
			{75.24, 76.269900, 75.060000},
			{75.17, 75.280000, 74.500000},
			{74.6, 75.310000, 74.540000},
			{74.1, 75.467000, 74.010000},
			{73.740000, 74.700000, 73.546000},
			{73.390000, 73.830000, 72.720000},
			{73.25, 73.890000, 72.86},
			{74.36, 74.410000, 73, 26},
			{76.510000, 76.830000, 74.820000},
			{75.590000, 76.850000, 74.540000},
			{75.910000, 76.960000, 75.510000},
			{74.610000, 77.070000, 74.560000},
			{75.330000, 75.530000, 74.010000},
			{75.010000, 75.500000, 74.510000},
			{75.620000, 76.210000, 75.250000},
			{76.040000, 76.460000, 75.092800},
			{76.450000, 76.450000, 75.435000},
			{76.260000, 76.470000, 75.840000},
			{76.850000, 77.000000, 76.190000},
	};

	private Candle newCandle(int i) {
		return CandleHelper.newCandle(i, prices[i][0], prices[i][0], prices[i][1], prices[i][2]);
	}

	private Candle newCandle1(int i) {
		double f = i % 2 == 0 ? 1.01 : 0.99;
		return CandleHelper.newCandle(i, prices[i][0] * f, prices[i][0] * f, prices[i][1] * 0.99, prices[i][2] * 1.01);
	}

	private Candle newCandle2(int i) {
		return CandleHelper.newCandle(i + 1, prices[i][0], prices[i][0], prices[i][1], prices[i][2]);
	}

	private double accumulateAndGetSar(ParabolicSAR sar, int i) {
		sar.accumulate(newCandle(i));
		return sar.getValue();
	}

	private double accumulateAndGetSar2(ParabolicSAR sar, int i) {
		sar.accumulate(newCandle1(i));
		sar.accumulate(newCandle2(i));
		return sar.getValue();
	}

	@Test
	public void testSar() {
		ParabolicSAR sar = new ParabolicSAR(minutes(1));

		int i = 0;
		assertEquals(0.0, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.640000000000000568434188608080, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.640000000000000568434188608080, accumulateAndGetSar(sar, i++), 0.001); // start with up trend
		assertEquals(76.269900000000006912159733474255, accumulateAndGetSar(sar, i++), 0.001); // switch to downtrend
		assertEquals(76.234502000000006773916538804770, accumulateAndGetSar(sar, i++), 0.001); // hold trend...
		assertEquals(76.200611960000006763493729522452, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(76.112987481600006697590288240463, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(75.958968232704006684543855953962, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(75.699850774087686058830877300352, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(75.461462712160671083174936939031, accumulateAndGetSar(sar, i++), 0.001); // switch to up trend
		assertEquals(72.719999999999998863131622783840, accumulateAndGetSar(sar, i++), 0.001);// hold trend
		assertEquals(72.802199999999998851762939011678, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(72.964111999999998670318746007979, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(73.203865279999998374933056766167, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(73.513156057599997959241591161117, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(73.797703572991997576805442804471, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.059487287152637224964186316356, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.300328304180425701270230347291, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.521902039845991099471790855751, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.725749876658311265817226523534, accumulateAndGetSar(sar, i++), 0.001);
		assertEquals(74.913289886525645818855027337894, accumulateAndGetSar(sar, i++), 0.001);
	}

	@Test
	public void testSar2() {
		ParabolicSAR sar = new ParabolicSAR(minutes(2));

		int i = 0;
		assertEquals(0.0, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.640000000000000568434188608080, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.640000000000000568434188608080, accumulateAndGetSar2(sar, i++), 0.001); // start with up trend
		assertEquals(76.269900000000006912159733474255, accumulateAndGetSar2(sar, i++), 0.001); // switch to downtrend
		assertEquals(76.234502000000006773916538804770, accumulateAndGetSar2(sar, i++), 0.001); // hold trend...
		assertEquals(76.200611960000006763493729522452, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(76.112987481600006697590288240463, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(75.958968232704006684543855953962, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(75.699850774087686058830877300352, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(75.461462712160671083174936939031, accumulateAndGetSar2(sar, i++), 0.001); // switch to up trend
		assertEquals(72.719999999999998863131622783840, accumulateAndGetSar2(sar, i++), 0.001);// hold trend
		assertEquals(72.802199999999998851762939011678, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(72.964111999999998670318746007979, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(73.203865279999998374933056766167, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(73.513156057599997959241591161117, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(73.797703572991997576805442804471, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.059487287152637224964186316356, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.300328304180425701270230347291, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.521902039845991099471790855751, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.725749876658311265817226523534, accumulateAndGetSar2(sar, i++), 0.001);
		assertEquals(74.913289886525645818855027337894, accumulateAndGetSar2(sar, i++), 0.001);
	}
}
