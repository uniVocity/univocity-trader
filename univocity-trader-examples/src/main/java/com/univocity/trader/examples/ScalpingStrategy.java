package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.*;

public class ScalpingStrategy extends IndicatorStrategy {
	private final Set<Indicator> indicators = new HashSet<>();
	private final BollingerBand boll5m;

	public ScalpingStrategy() {
		indicators.add(boll5m = new BollingerBand(TimeInterval.minutes(5)));
		boll5m.recalculateEveryTick(true);
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public Signal getSignal(Candle candle, Context context) {
		if (candle.close < boll5m.getLowerBand()) { //close price of the candle is under the lower band.
			return Signal.BUY;
		}

		if (candle.close > boll5m.getUpperBand()) { //close of the candle is above the upper band
			return Signal.SELL;
		}
		return Signal.NEUTRAL;
	}
}
