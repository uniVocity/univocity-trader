package com.univocity.trader.examples;

import java.util.*;

import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

public class ExampleStrategyMonitor extends StrategyMonitor {
	private final Set<Indicator> indicators = new HashSet<>();
	private final InstantaneousTrendline trend;
	private boolean waitForUptrend = false;

	public ExampleStrategyMonitor() {
		indicators.add(trend = new InstantaneousTrendline(TimeInterval.minutes(25)));
	}

	@Override
	public boolean discardBuy(Strategy strategy) {
		if (trend.getSignal(trader.latestCandle()) == Signal.BUY) {
			waitForUptrend = false;
		}
		return waitForUptrend;
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public String handleStop(Signal signal, Strategy strategy) {
		final double currentReturns = trader.priceChangePct();
		final double bestReturns = trader.maxChange();
		if ((currentReturns - bestReturns) < -2.0) {
			if (currentReturns < 0.0) {
				waitForUptrend = true;
				return "stop loss";
			}
			return "exit with some profit";
		}
		return null;
	}
}
