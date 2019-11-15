package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

import java.util.*;

public abstract class StrategyMonitor extends IndicatorGroup {

	protected Trader trader;

	public String handleStop(Signal signal, Strategy strategy) {
		return null;
	}

	public boolean discardBuy(Strategy strategy) {
		return false;
	}

	public boolean allowMixedStrategies() {
		return true;
	}

	public void highestProfit(double change) {

	}

	public void worstLoss(double change) {

	}

	public void bought() {

	}

	public void sold() {

	}

	public boolean allowSelling() {
		return true;
	}

	public boolean allowTradeSwitch(String exitSymbol, Candle candle, String candleTicker){
		return false;
	}

	public void setTrader(Trader trader) {
		this.trader = trader;
	}
}
