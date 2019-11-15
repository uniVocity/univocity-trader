package com.univocity.trader.exchange.binance.example;

import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.*;

public class ExampleStrategyMonitor extends StrategyMonitor {

	private final Set<Indicator> indicators = new HashSet<>();

	private final InstantaneousTrendline trend;
	private boolean waitForUptrend = false;

	public ExampleStrategyMonitor() {
		indicators.add(trend = new InstantaneousTrendline(TimeInterval.minutes(25)));
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public String handleStop(Signal signal, Strategy strategy) {
		double currentReturns = trader.getChange();
		double bestReturns = trader.getMaxChange();

		if (currentReturns - bestReturns < -2.0) {
			if (currentReturns < 0.0) {
				waitForUptrend = true;
				return "stop loss";
			}
			return "exit with some profit";
		}
		return null;
	}

	@Override
	public boolean discardBuy(Strategy strategy) {
		if (trend.getSignal(trader.getCandle()) == Signal.BUY) {
			waitForUptrend = false;
		}

		return waitForUptrend;
	}
}
