package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.strategy.*;

import java.util.*;

public class ScalpingStrategyMonitor extends StrategyMonitor {
	private final Set<Indicator> indicators = new HashSet<>();


	public ScalpingStrategyMonitor() {
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public String handleStop(Trade trade, Signal signal, Strategy strategy) {
		double averagePricePaid = trade.averagePrice();
		double currentTickerPrice = trade.lastClosingPrice();

		int pipSize = trader.pipSize();

		//75 pips either way, exit.
		double priceMovement = 75.0 / Math.pow(10, pipSize);

		double difference = Math.abs(averagePricePaid - currentTickerPrice);
		if(difference > priceMovement){
			if(averagePricePaid > currentTickerPrice){
				return "stop loss";
			} else {
				return "take profit";
			}
		}

		return null;
	}
}
