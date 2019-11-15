package com.univocity.trader.candles;

import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class AggregatorTest {

	private Aggregator parent = new Aggregator("test");

	public static Candle[] SEQUENCE = new Candle[]{
			new Candle(1527759000000L, 1527759059999L, 0.2802100000, 0.3060000000, 0.2802100000, 0.3060000000, 4422.7100000000),
			new Candle(1527759060000L, 1527759119999L, 0.3058000000, 0.3058000000, 0.2816100000, 0.2940000000, 25995.1300000000),
			new Candle(1527759120000L, 1527759179999L, 0.2950000000, 0.2980000000, 0.2860000000, 0.2950000000, 87119.7400000000),
			new Candle(1527759180000L, 1527759239999L, 0.2950000000, 0.2950000000, 0.2921100000, 0.2950000000, 15028.1500000000),
			new Candle(1527759240000L, 1527759299999L, 0.2949900000, 0.2971000000, 0.2949400000, 0.2970100000, 56145.0600000000),
			new Candle(1527759300000L, 1527759359999L, 0.2969700000, 0.2970000000, 0.2953500000, 0.2954200000, 22982.0500000000),
			new Candle(1527759360000L, 1527759419999L, 0.2954200000, 0.2954200000, 0.2950000000, 0.2950000000, 94372.2200000000),
			new Candle(1527759420000L, 1527759479999L, 0.2951100000, 0.2983100000, 0.2950000000, 0.2953100000, 42722.4600000000),
			new Candle(1527759480000L, 1527759539999L, 0.2953000000, 0.2953000000, 0.2947700000, 0.2948700000, 35073.5000000000),
			new Candle(1527759540000L, 1527759599999L, 0.2948700000, 0.2950000000, 0.2948700000, 0.2949100000, 6449.6900000000)
	};

	@Test
	public void testAggregation1Min() {
		Aggregator a = Aggregator.getInstance(parent, minutes(1));

		Candle full = a.getFull();
		Candle partial = a.getPartial();

		assertNull(full);
		assertNull(partial);

		a.aggregate(SEQUENCE[0]);
		assertNotNull(full = a.getFull());
		assertEquals(full, SEQUENCE[0]);
		assertNull(partial = a.getPartial());

		a.aggregate(SEQUENCE[1]);
		assertNotNull(full = a.getFull());
		assertEquals(full, SEQUENCE[1]);
		assertNull(partial = a.getPartial());
	}

	@Test
	public void testAggregation3Min() {
		Aggregator a = Aggregator.getInstance(parent, minutes(3));
		Candle full = a.getFull();
		Candle partial = a.getPartial();

		assertNull(full);
		assertNull(partial);

		a.aggregate(SEQUENCE[0]);
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());

		a.aggregate(SEQUENCE[1]);
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());

		assertEquals(1527759000000L, partial.openTime);
		assertEquals(1527759119999L, partial.closeTime);
		assertEquals(0.2802100000, partial.open);
		assertEquals(0.3060000000, partial.high);
		assertEquals(0.2802100000, partial.low);
		assertEquals(0.2940000000, partial.close);
		assertEquals(4422.7100000000 + 25995.1300000000, partial.volume);

		a.aggregate(SEQUENCE[2]);
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());

		assertEquals(1527759000000L, full.openTime);
		assertEquals(1527759179999L, full.closeTime);
		assertEquals(0.2802100000, full.open);
		assertEquals(0.3060000000, full.high);
		assertEquals(0.2802100000, full.low);
		assertEquals(0.2950000000, full.close);
		assertEquals(4422.7100000000 + 25995.1300000000 + 87119.7400000000, full.volume);

		a.aggregate(SEQUENCE[3]);
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());

		a.aggregate(SEQUENCE[4]);
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());

		a.aggregate(SEQUENCE[5]);
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());

		assertEquals(1527759180000L, full.openTime);
		assertEquals(1527759359999L, full.closeTime);
		assertEquals(0.2950000000, full.open);
		assertEquals(0.2971000000, full.high);
		assertEquals(0.2921100000, full.low);
		assertEquals(0.2954200000, full.close);
		assertEquals(15028.1500000000 + 56145.0600000000 + 22982.0500000000, full.volume);

		a.aggregate(SEQUENCE[6]);
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());

	}

	@Test
	public void testAggregation2Min() {
		Aggregator a = Aggregator.getInstance(parent, minutes(2));
		Candle full = a.getFull();
		Candle partial = a.getPartial();

		assertNull(full);
		assertNull(partial);

		a.aggregate(newCandle(1, 1));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(1.0, partial.high);
		assertEquals(1.0, partial.low);
		assertEquals(1.0, partial.close);


		a.aggregate(newCandle(2, 2));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(2.0, full.high);
		assertEquals(1.0, full.low);
		assertEquals(2.0, full.close);
		//end candle 1

		a.aggregate(newCandle(3, 3));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(3.0, partial.high);
		assertEquals(3.0, partial.low);
		assertEquals(3.0, partial.close);

		a.aggregate(newCandle(4, 4));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(4.0, full.high);
		assertEquals(3.0, full.low);
		assertEquals(4.0, full.close);
		//end candle 2

		a.aggregate(newCandle(5, 3));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(3.0, partial.high);
		assertEquals(3.0, partial.low);
		assertEquals(3.0, partial.close);

		a.aggregate(newCandle(6, 4));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(4.0, full.high);
		assertEquals(3.0, full.low);
		assertEquals(4.0, full.close);
		//end candle 3

		a.aggregate(newCandle(7, 5));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(5.0, partial.high);
		assertEquals(5.0, partial.low);
		assertEquals(5.0, partial.close);

		a.aggregate(newCandle(8, 4));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(5.0, full.high);
		assertEquals(4.0, full.low);
		assertEquals(4.0, full.close);
		//end candle 4

		a.aggregate(newCandle(9, 3));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(3.0, partial.high);
		assertEquals(3.0, partial.low);
		assertEquals(3.0, partial.close);

		a.aggregate(newCandle(10, 3));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(3.0, full.high);
		assertEquals(3.0, full.low);
		assertEquals(3.0, full.close);
		//end candle 5

		a.aggregate(newCandle(11, 4));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(4.0, partial.high);
		assertEquals(4.0, partial.low);
		assertEquals(4.0, partial.close);

		a.aggregate(newCandle(12, 3));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(4.0, full.high);
		assertEquals(3.0, full.low);
		assertEquals(3.0, full.close);
		//end candle 6

		a.aggregate(newCandle(13, 2));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(2.0, partial.high);
		assertEquals(2.0, partial.low);
		assertEquals(2.0, partial.close);

		a.aggregate(newCandle(14, 1));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(2.0, full.high);
		assertEquals(1.0, full.low);
		assertEquals(1.0, full.close);
		//end candle 7
	}

	@Test
	public void testTickAggregation() {
		Aggregator a = Aggregator.getInstance(parent, millis(2500)); //2.5 seconds
		Candle full = a.getFull();
		Candle partial = a.getPartial();

		assertNull(full);
		assertNull(partial);

		//ticks with VARYING open and close times
		a.aggregate(newTick(0L, 1000L, 1)); //1 sec
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(1.0, partial.high);
		assertEquals(1.0, partial.low);
		assertEquals(1.0, partial.close);


		a.aggregate(newTick(1500L, 2200L, 2)); //1.2 sec
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(2.0, partial.high);
		assertEquals(1.0, partial.low);
		assertEquals(2.0, partial.close);

		a.aggregate(newTick(5000L, 8000L, 3)); //3 sec
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(3.0, full.high);
		assertEquals(1.0, full.low);
		assertEquals(3.0, full.close);
		//end candle

		//ticks with SAME opentime and closetime
		a.aggregate(newTick(1000L, 1000L, 4));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(4.0, partial.high);
		assertEquals(4.0, partial.low);
		assertEquals(4.0, partial.close);

		a.aggregate(newTick(1500, 1500, 3));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(4.0, partial.high);
		assertEquals(3.0, partial.low);
		assertEquals(3.0, partial.close);

		a.aggregate(newTick(3500, 3500, 3.5));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(4.0, full.high);
		assertEquals(3.0, full.low);
		assertEquals(3.5, full.close);
		//end candle

		//Ticks with FIXED open time
		a.aggregate(newTick(1000, 1500, 5.0));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(5.0, partial.high);
		assertEquals(5.0, partial.low);
		assertEquals(5.0, partial.close);

		a.aggregate(newTick(1000, 2000, 4));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(5.0, partial.high);
		assertEquals(4.0, partial.low);
		assertEquals(4.0, partial.close);


		a.aggregate(newTick(1000, 3000, 3));
		assertNull(full = a.getFull());
		assertNotNull(partial = a.getPartial());
		assertEquals(5.0, partial.high);
		assertEquals(3.0, partial.low);
		assertEquals(3.0, partial.close);

		a.aggregate(newTick(1000, 3500, 2));
		assertNotNull(full = a.getFull());
		assertNull(partial = a.getPartial());
		assertEquals(5.0, full.high);
		assertEquals(2.0, full.low);
		assertEquals(2.0, full.close);
		//end candle
	}
}