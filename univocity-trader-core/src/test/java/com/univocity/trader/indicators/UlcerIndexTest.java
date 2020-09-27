package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;
import static org.junit.Assert.*;

public class UlcerIndexTest {

	private static final double[] values = {194.75, 195.00, 195.10, 194.46, 190.60, 188.86, 185.47, 184.46, 182.31,
			185.22, 184.00, 182.87, 187.45, 194.51, 191.63, 190.02, 189.53, 190.27, 193.13, 195.55, 195.84, 195.15,
			194.35, 193.62, 197.68, 197.91, 199.08, 199.03, 198.42, 199.29, 199.01, 198.29, 198.40, 200.84, 201.22,
			200.50, 198.65, 197.25, 195.70, 197.77, 195.69, 194.87, 195.08};

	@Test
	public void ulcer() {
		int i = 0;

		UlcerIndex indicator = new UlcerIndex(14, minutes(1));

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		assertEquals(0, indicator.getValue(), 0.0001);

		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));
		indicator.accumulate(CandleHelper.newCandle(i, values[i++]));

		assertEquals(26, i);
		assertEquals(1.234009, indicator.getValue(), 0.0001);
		accumulateAndTest(1.2340096463740846, indicator, i, values[i++]);
		accumulateAndTest(0.560553282860879, indicator, i, values[i++]);
		accumulateAndTest(0.39324888828140886, indicator, i, values[i++]);
		accumulateAndTest(0.38716275079310825, indicator, i, values[i++]);
		accumulateAndTest(0.3889794194862251, indicator, i, values[i++]);
		accumulateAndTest(0.4114481689096125, indicator, i, values[i++]);
		accumulateAndTest(0.42841008722557894, indicator, i, values[i++]);
		accumulateAndTest(0.42841008722557894, indicator, i, values[i++]);
		accumulateAndTest(0.3121617589229034, indicator, i, values[i++]);
		accumulateAndTest(0.2464924497436544, indicator, i, values[i++]);
		accumulateAndTest(0.4089008481549337, indicator, i, values[i++]);
		accumulateAndTest(0.667264629592715, indicator, i, values[i++]);
		accumulateAndTest(0.9913518177402276, indicator, i, values[i++]);
		accumulateAndTest(1.0921325741850083, indicator, i, values[i++]);
		accumulateAndTest(1.3156949266800984, indicator, i, values[i++]);
		accumulateAndTest(1.5606676136361992, indicator, i, values[i++]);
	}

	private void accumulateAndTest(double expected, UlcerIndex indicator, int i, double close) {
		indicator.accumulate(CandleHelper.newCandle(i, 0, close, 0, 0));
		double current = indicator.getValue();
		assertEquals(expected, current, 0.0001);
	}
}