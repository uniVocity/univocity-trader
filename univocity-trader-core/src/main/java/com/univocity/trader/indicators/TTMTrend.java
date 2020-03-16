package com.univocity.trader.indicators;


import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

import static com.univocity.trader.indicators.Signal.*;

public class TTMTrend extends SingleValueIndicator {

	private SingleValueIndicator avg;
	private int trendLength;
	private Signal currentTrend = NEUTRAL;

	public TTMTrend(TimeInterval interval) {
		this(5, interval);
	}

	public TTMTrend(int length, TimeInterval interval) {
		this(length, interval, c -> (c.high + c.low) / 2.0);
	}

	public TTMTrend(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
		super(interval, valueGetter);
		this.avg = new MovingAverage(length, interval);
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		if (updating) {
			avg.update(value);
		} else {
			avg.accumulate(value);
		}
		updateTrend(value);
		return true;
	}

	private void updateTrend(double value) {
		double avg = this.avg.getValue();
		if (value > avg) {
			if (currentTrend == BUY) {
				trendLength++;
			} else {
				trendLength = 1;
				currentTrend = BUY;
				return;
			}
		} else if (value < avg) {
			if (currentTrend == SELL) {
				trendLength++;
			} else {
				trendLength = 1;
				currentTrend = SELL;
				return;
			}
		} else {
			trendLength = 1;
			currentTrend = NEUTRAL;
		}
	}

	public int getTrendLength() {
		return trendLength;
	}

	@Override
	public double getValue() {
		return 0;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{avg};
	}
}
