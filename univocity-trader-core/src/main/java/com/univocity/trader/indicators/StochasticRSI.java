package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class StochasticRSI extends SingleValueIndicator {

	private final RSI rsi;
	private final LowestValueIndicator minRsi;
	private final HighestValueIndicator maxRsi;
	private double value;

	public StochasticRSI(TimeInterval interval) {
		this(14, interval);
	}

	public StochasticRSI(int length, TimeInterval interval) {
		super(interval, null);
		this.rsi = new RSI(length, interval);
		this.minRsi = new LowestValueIndicator(length, interval, null);
		this.maxRsi = new HighestValueIndicator(length, interval, null);
	}

	public double getValue() {
		return value;
	}

	@Override
	protected boolean process(Candle candle, double value, boolean updating) {
		rsi.update(candle);

		double rsi = this.rsi.getValue();

		if (updating) {
			minRsi.update(rsi);
			maxRsi.update(rsi);
		} else {
			minRsi.accumulate(rsi);
			maxRsi.accumulate(rsi);
		}

		this.value = ((rsi - minRsi.getValue()) / (maxRsi.getValue() - minRsi.getValue())) * 100;
		return true;
	}

	@Override
	protected Indicator[] children() {
		return new Indicator[]{rsi, minRsi, maxRsi};
	}
}
