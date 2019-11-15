package com.univocity.trader.indicators;




import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.candles.CandleHelper.*;
import static com.univocity.trader.indicators.base.TimeInterval.*;
import static junit.framework.TestCase.*;


public class AverageTrueRangeTest {

	public static final double[][] prices = new double[][]{
			//High, Low, Close, ATR, TR
			{229.7, 219.5, 224.8, 10.2, 10.2},
			{228, 217.5, 219.4, 10.2230769230769, 10.5},
			{237.5, 216.6, 237, 11.0443786982248, 20.9},
			{243.2, 234, 240.2, 10.902503413746, 9.19999999999999},
			{243.5, 232, 242.5, 10.9484646896117, 11.5},
			{257.55, 240, 247.3, 11.4562750981031, 17.55},
			{253.3, 240.5, 250.6, 11.5596385520952, 12.8},
			{258.8, 248.5, 255.7, 11.4627432788571, 10.3},
			{257, 242, 243.2, 11.7348399497142, 15},
			{246.9, 235, 245.9, 11.747544568967, 11.9},
			{250.1, 242, 248, 11.466964217508, 8.09999999999999},
			{249, 243.5, 246.4, 11.0079669700074, 5.5},
			{252, 241, 243.8, 11.0073541261607, 11},
			{246, 241.3, 244.3, 10.5221730395329, 4.69999999999999},
			{245.25, 234.9, 235.6, 10.5089289595688, 10.35},
			{244.3, 231.8, 239, 10.6620882703712, 12.5},
			{248.8, 237.8, 248.5, 10.6880814803427, 11},
			{251.5, 246.2, 250.3, 10.2736136741625, 5.30000000000001},
			{255.3, 246.1, 249.8, 10.1910280069192, 9.20000000000002},
			{274.9, 250.1, 274.4, 11.337872006387, 25.1},
			{281.5, 267.5, 275.3, 11.5426510828187, 14},
			{282, 269.5, 276.9, 11.6162933072173, 12.5},
			{294.75, 275.3, 284.6, 12.218886129739, 19.45},
	};

	static final double[][] merged = new double[][]{
			//High, Low, Close, TR, ATR
			{229.70, 217.5, 219.4, 12.2, 12.2},
			{243.20, 216.6, 240.2, 26.6, 15.08},
			{257.55, 232.0, 247.3, 25.55, 17.174},
			{258.80, 240.5, 255.7, 18.3, 17.3992},
			{257.00, 235.0, 245.9, 22, 18.31936},
			{250.10, 242.0, 246.4, 8.09, 16.275488},
			{252.00, 241.0, 244.3, 11, 15.2203904},
			{245.25, 231.8, 239.0, 13.45, 14.86631232},
			{251.50, 237.8, 250.3, 13.7, 14.633049856},
			{274.90, 246.1, 274.4, 28.8, 17.4664398848},
			{282.00, 267.5, 276.9, 14.5, 16.87315190784},
			{294.75, 275.3, 284.6, 19.45, 17.388521526272},
	};

	@Test
	public void getValue() {
		AverageTrueRange t = new AverageTrueRange(13, minutes(1));

		for (int i = 0; i < prices.length; i++) {
			Candle c = newCandle(i, prices[i][2], prices[i][2], prices[i][0], prices[i][1]);

			t.accumulate(c);
			assertEquals("ATR candle " + i, prices[i][3], t.getValue(), 0.0001);
		}
	}

	@Test
	public void getValueInterval2() {
		AverageTrueRange t = new AverageTrueRange(5, minutes(2));

		int m = 0;
		for (int i = 0; i < prices.length; i++){
			Candle c = newCandle(i, prices[i][2], prices[i][2], prices[i][0], prices[i][1]);

			t.accumulate(c);
			if (i % 2 == 0) {
				continue;
			}

			assertEquals("ATR candle " + i, merged[m][4], t.getValue(), 0.0001);
			m++;
		}
	}


	private Candle newCandle1(int i) {
		double f = i % 2 == 0 ? 1.01 : 0.99;
		return newCandle(i, prices[i][2], prices[i][2] * f, prices[i][0] * 0.99, prices[i][1] * 1.01);
	}

	private Candle newCandle2(int i) {
		return newCandle(i + 1, prices[i][2] * 0.99, prices[i][2], prices[i][0], prices[i][1]);
	}


	@Test
	public void getValueInterval3() {
		AverageTrueRange t = new AverageTrueRange(13, minutes(2));

		for (int i = 0; i < prices.length; i++){
			t.accumulate(newCandle1(i));
			t.accumulate(newCandle2(i));
			assertEquals("ATR candle " + i, prices[i][3], t.getValue(), 0.0001);
		}
	}
}