package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.*;

import static com.univocity.trader.indicators.base.TimeInterval.*;

public class ScalpingStrategyMonitor extends StrategyMonitor {
	private final Set<Indicator> indicators = new HashSet<>();

	private AggregatedTicksIndicator fifteenSecondAggregate;

	public ScalpingStrategyMonitor() {
		indicators.add(fifteenSecondAggregate = new AggregatedTicksIndicator(seconds(15)));
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public String handleStop(Trade trade) {
		double averagePricePaid = trade.averagePrice();
		double currentTickerPrice = trade.lastClosingPrice();

		int pipSize = trader.pipSize();

		//75 pips either way, exit.
		double priceMovement = 75.0 / Math.pow(10, pipSize);

		double difference = Math.abs(averagePricePaid - currentTickerPrice);
		if (difference > priceMovement) {
			Candle candle = fifteenSecondAggregate.getLastFullCandle();

			if (averagePricePaid > currentTickerPrice) {
				if (candle.open > candle.close) { //15 second candle closing lower than open, get out.
					return "stop loss";
				}
			} else {
				if (candle.open > candle.close) { //15 second candle closing lower than open, get out.
					return "take profit";
				}
			}
		}

		return null;
	}
}
