package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.junit.*;

import static org.junit.Assert.*;

public class CovarianceTest {

	private CloseVolume[] candles = {new CloseVolume(6, 100),
			new CloseVolume(7, 105),
			new CloseVolume(9, 130),
			new CloseVolume(12, 160),
			new CloseVolume(11, 150),
			new CloseVolume(10, 130),
			new CloseVolume(11, 95),
			new CloseVolume(13, 120),
			new CloseVolume(15, 180),
			new CloseVolume(12, 160),
			new CloseVolume(8, 150),
			new CloseVolume(4, 200),
			new CloseVolume(3, 150),
			new CloseVolume(4, 85),
			new CloseVolume(3, 70),
			new CloseVolume(5, 90),
			new CloseVolume(8, 100),
			new CloseVolume(9, 95),
			new CloseVolume(11, 110),
			new CloseVolume(10, 95)
	};


	@Test
	public void usingBarCount5UsingClosePriceAndVolume() {

		int i = 0;

		TimeInterval interval = TimeInterval.MINUTE;

		Volume volume = new Volume(2, interval);
		Covariance indicator = new Covariance(5, volume, new FunctionIndicator(interval, c -> c.close));

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(26.25, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(63.3333, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(143.75, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(156, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(60.8, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(15.2, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(-17.6, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(4, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(11.6, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(-14.4, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(-100.2, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(-70.0, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(24.6, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(35.0, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(-19.0, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(-47.8, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(11.4, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(55.8, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(33.4, indicator.getValue(), 0.0001);


	}

	@Test
	public void firstValueShouldBeZero() {

		int i = 0;

		TimeInterval interval = TimeInterval.MINUTE;

		Volume volume = new Volume(2, interval);
		Covariance indicator = new Covariance(5, volume, c -> c.close);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(0, indicator.getValue(), 0.0001);

	}

	@Test
	public void shouldBeZeroWhenBarCountIs1() {

		int i = 0;

		TimeInterval interval = TimeInterval.MINUTE;

		Volume volume = new Volume(2, interval);
		Covariance indicator = new Covariance(1, volume, c -> c.close);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(candles[i].getCandle(i++));
		assertEquals(0, indicator.getValue(), 0.0001);


	}

	class CloseVolume {

		double close;
		double volume;

		public CloseVolume(double close, double volume) {
			this.close = close;
			this.volume = volume;
		}

		public Candle getCandle(int i) {
			return CandleHelper.newCandle(i, 0, close, 0, 0, volume);
		}

	}

}
