package com.univocity.trader.examples;

import java.util.*;

import org.slf4j.*;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.strategy.*;

public class ProfitabilityStrategyMonitor extends StrategyMonitor {
	/*
	 * 1% ensures that we don't make losing sales, and lets the strategy class
	 * determine to sell or not
	 */
	private static final double MINIMUM_PROFIT = 0.01;
	private static final Logger log = LoggerFactory.getLogger(ProfitabilityStrategyMonitor.class);

	public ProfitabilityStrategyMonitor() {
	}

	@Override
	protected Set<Indicator> getAllIndicators() {
		return null;
	}

	@Override
	public boolean allowExit(Trade trade) {
		TradingFees tradingFees = trader.tradingFees();
		double quantity = this.trader.assetQuantity();
		double price = trade.averagePrice();
		double purchaseValue = price * quantity;
		double sellValue = tradingFees.takeFee(trader.latestCandle().low * quantity, Order.Type.MARKET,
				Order.Side.SELL);
		double diffpct = (sellValue - purchaseValue) / purchaseValue;
		if (diffpct < MINIMUM_PROFIT) {
			/*
			 * reject sales where we make less than MINIMUM_PROFIT profit
			 */
			return true;
		}
		log.debug("trade profitability of {}% accepted", String.format("%.3f", diffpct * 100));
		return false;
	}
}