package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.*;

public class ExampleStrategy extends IndicatorStrategy {
	private final Set<Indicator> indicators = new HashSet<>();
	private final BollingerBand boll5m;
	private final BollingerBand boll1h;

	public ExampleStrategy() {
		indicators.add(boll5m = new BollingerBand(TimeInterval.minutes(5)));
		indicators.add(boll1h = new BollingerBand(TimeInterval.hours(1)));
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public Signal getSignal(Candle candle, Context context) {
		if (candle.high < boll1h.getLowerBand()) { // price jumped below lower band on the 1 hour time frame
			if (candle.low > boll5m.getLowerBand()) { // on the 5 minute time frame, the lowest price of the candle is above the lower band.
				if (candle.close < boll5m.getMiddleBand()) { // still on the 5 minute time frame, the close price of the candle is under the middle band
					if (boll5m.movingUp()) { // if the slope of the 5 minute bollinger band is starting to point up, BUY
						return Signal.BUY;
					}
				}
			}
		}
		if (candle.high > boll1h.getUpperBand()) { // candle hitting the upper band on the 1 hour time frame
			if (candle.low < boll5m.getMiddleBand()) { // on the 5 minute time frame, the lowest price of the candle is under the middle band
				if (boll5m.movingDown()) { // if the slope of the 5 minute bollinger band is starting to point down, SELL
					return Signal.SELL;
				}
			}
		}
		return Signal.NEUTRAL;
	}
}
