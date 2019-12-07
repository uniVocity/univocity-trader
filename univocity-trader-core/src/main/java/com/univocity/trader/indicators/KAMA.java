package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.util.function.*;

public class KAMA extends SingleValueCalculationIndicator {

	private final double fastest;
	private final double slowest;

	private final CircularList values;
	private final CircularList volatility;

	public KAMA(TimeInterval interval) {
		this(10, 2, 30, interval, c -> c.close);
	}

	public KAMA(TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		this(10, 2, 30, interval, valueGetter);
	}

	public KAMA(int barCountEffectiveRatio, int barCountFast, int barCountSlow, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);

		fastest = 2.0 / (barCountFast + 1.0);
		slowest = 2.0 / (barCountSlow + 1.0);

		this.values = new CircularList(barCountEffectiveRatio);
		this.volatility = new CircularList(barCountEffectiveRatio);
	}

	@Override
	protected double calculate(Candle candle, double value, double previousValue, boolean updating) {
		// Change = ABS(Close - Close (10 periods ago))
		double change = Math.abs(value - values.first());

		// Volatility = Sum10(ABS(Close - Prior Close)), i.e. the sum of the absolute value of the last ten price changes (Close - Prior Close).
		volatility.accumulate(Math.abs(value - values.last()), updating);
		values.accumulate(value, updating);

		if (getAccumulationCount() < values.capacity()) {
			return value;
		}

		// Efficiency Ratio (ER) ER = Change/Volatility
		double er = change / volatility.sum();

		// Smoothing Constant (SC) SC = [ER x (fastest SC - slowest SC) + slowest SC]2
		double sc = Math.pow(er * (fastest - slowest) + slowest, 2.0);

		//KAMA Current KAMA = Prior KAMA + SC x (Price - Prior KAMA)
		return previousValue + (sc * (value - previousValue));
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[0];
	}
}

