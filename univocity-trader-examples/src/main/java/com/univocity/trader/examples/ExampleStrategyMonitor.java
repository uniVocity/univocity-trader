package com.univocity.trader.examples;

import com.univocity.trader.account.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.*;

public class ExampleStrategyMonitor extends StrategyMonitor {
	private final Set<Indicator> indicators = new HashSet<>();
	private final InstantaneousTrendline trend;
	private boolean waitForUptrend = false;
	private boolean waitForDowntrend = false;

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
	public boolean discardShortSell(Strategy strategy) {
		if (trend.getSignal(trader.latestCandle()) == Signal.SELL) {
			waitForDowntrend = false;
		}
		return waitForDowntrend;
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return indicators;
	}

	@Override
	public String handleStop(Trade trade) {
		final double currentReturns = trade.priceChangePct();
		final double bestReturns = trade.maxChange();
		if(trade.isLong()) {
			if ((currentReturns - bestReturns) < -2.0) {
				if (currentReturns < 0.0) {
					waitForUptrend = true;
					return "stop loss on long position";
				}
				return "exit long with some profit";
			}
		} else if(trade.isShort()){
			if ((currentReturns - bestReturns) < -2.0) {
				if (currentReturns < 0.0) {
					waitForDowntrend = true;
					return "stop loss on short position";
				}
				return "exit short with some profit";
			}
		}
		return null;
	}
}
