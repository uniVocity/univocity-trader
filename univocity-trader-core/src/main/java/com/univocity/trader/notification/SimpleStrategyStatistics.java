package com.univocity.trader.notification;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.text.*;
import java.util.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.candles.Candle.*;

public class SimpleStrategyStatistics implements OrderListener {

	private Map<String, List<Double>> parameterReturns = new TreeMap<>();
	private double initialInvestment = 0.0;
	private Trader trader;

	private final String symbol;

	private Candle firstCandle;
	private Candle lastCandle;

	public SimpleStrategyStatistics() {
		this(null);
	}

	public SimpleStrategyStatistics(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public void onOrder(Order order, Trader trader, Client client) {
		if (this.trader == null) {
			this.trader = trader;
			initialInvestment = this.trader.getTotalFundsInReferenceCurrency();
			firstCandle = this.trader.getCandle();
		}
		if (order.getSide() == SELL) {
			double change = trader.getPriceChangePct() - trader.getBreakEvenChange(order.getTotalSpent().doubleValue());
			if (!Double.isNaN(change)) {
				parameterReturns.computeIfAbsent(trader.getParameters().toString(), s -> new ArrayList<>()).add(change);
			}
		}
		lastCandle = trader.getCandle();
	}

	public void printTradeStats() {
		for (Map.Entry<String, List<Double>> e : parameterReturns.entrySet()) {
			double totalNegative = 0.0;
			double totalPositive = 0.0;
			int negativeCount = 0;
			int positiveCount = 0;

			for (Double v : e.getValue()) {
				if (v <= 0.0) {
					totalNegative += v;
					negativeCount++;

				} else {
					totalPositive += v;
					positiveCount++;
				}
			}

			double averageGain = totalPositive / positiveCount;
			double averageLoss = totalNegative / negativeCount;

			double latestHoldings = trader.getTotalFundsInReferenceCurrency();

			System.out.println("===[ " + (symbol == null ? "" : symbol) + " results using parameters: " + e.getKey() + " ]===");
			DecimalFormat formatter = CHANGE_FORMAT.get();
			System.out.println("Negative: " + negativeCount + " trades, avg. loss: " + formatter.format(averageLoss / 100));
			System.out.println("Positive: " + positiveCount + " trades, avg. gain: +" + formatter.format(averageGain / 100));
			System.out.println("Returns : " + formatter.format((latestHoldings / initialInvestment) - 1.0));
			if (symbol != null) {
				double buyAndHold = (initialInvestment / firstCandle.close) * lastCandle.close;
				System.out.println("Buy&Hold: " + formatter.format((buyAndHold / initialInvestment) - 1.0));
			}
		}
	}
}

