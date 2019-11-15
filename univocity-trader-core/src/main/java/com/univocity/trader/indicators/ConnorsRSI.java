package com.univocity.trader.indicators;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class ConnorsRSI extends SingleValueIndicator {

	private RSI rsi;
	private StreakIndicator streak;
	private RSI streakRsi;
	private PercentRankIndicator rank;

	private double value;

		@Override
	protected Indicator[] children() {
		return new Indicator[]{rsi, streakRsi, streak, rank};
	}

	public ConnorsRSI(TimeInterval interval) {
		this(3, 2, 100, interval);
	}

	public ConnorsRSI(int rsiLength, int streakRsiLength, int pctRankLength, TimeInterval interval) {
		super(interval, null);
		rsi = new RSI(rsiLength, interval);

		streak = new StreakIndicator(TimeInterval.millis(1));
		streakRsi = new RSI(streakRsiLength, TimeInterval.millis(1));

		rank = new PercentRankIndicator(pctRankLength, TimeInterval.millis(1));
	}

	public double getValue() {
		return value;
	}

	@Override
	protected boolean process(Candle c, double value, boolean updating) {
		if (rsi.accumulate(c)) {
			streak.accumulate(c);
			rank.accumulate(c);

			//FIXME: what a frigging ugly hack.
			c = new Candle(c.openTime, c.closeTime, c.open, c.high, c.low, streak.getValue(), c.volume);
			streakRsi.accumulate(c);
			this.value = (rsi.getValue() + streakRsi.getValue() + rank.getValue()) / 3.0;

			return true;
		}
		return false;
	}
}
