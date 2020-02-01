package com.univocity.trader.notification;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.text.*;
import java.util.*;

import static com.univocity.trader.candles.Candle.*;

public class SimpleStrategyStatistics implements OrderListener {

	private Map<String, List<double[]>> longReturns = new TreeMap<>();
	private Map<String, List<double[]>> shortReturns = new TreeMap<>();
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

	private void register(Map<String, List<double[]>> returns, Trade trade) {
		returns.computeIfAbsent(trader.parameters().toString(), s -> new ArrayList<>())
				.add(new double[] { trade.actualProfitLoss(), trade.actualProfitLossPct() });
	}

	@Override
	public void orderFinalized(Order order, Trade trade, Client client) {
		if (order.getFillPct() == 0.0) {
			return;
		}
		if (this.trader == null) {
			this.trader = trade.trader();
			initialInvestment = this.trader.totalFundsInReferenceCurrency();
			firstCandle = this.trader.latestCandle();
		}
		if (order.isLongSell()) {
			register(longReturns, trade);
		} else if (order.isShortCover()) {
			register(shortReturns, trade);
		}
		lastCandle = trader.latestCandle();
	}

	private void printTradeStats() {
		printTradeStats(longReturns);
		printTradeStats(shortReturns);
	}

	private void printTradeStats(Map<String, List<double[]>> returns) {
		for (Map.Entry<String, List<double[]>> e : returns.entrySet()) {
			double totalNegativeAmount = 0.0;
			double totalPositiveAmount = 0.0;
			double totalNegativePct = 0.0;
			double totalPositivePct = 0.0;
			int negativeCount = 0;
			int positiveCount = 0;

			for (double[] v : e.getValue()) {
				double returnPct = v[1];
				double returnAmount = v[0];
				if (returnAmount <= 0.0) {
					totalNegativePct += returnPct;
					totalNegativeAmount += returnAmount;
					negativeCount++;

				} else {
					totalPositivePct += returnPct;
					totalPositiveAmount += returnAmount;
					positiveCount++;
				}
			}

			double averageGainPct = positiveCount == 0 ? 0 : totalPositivePct / positiveCount;
			double averageLossPct = negativeCount == 0 ? 0 : totalNegativePct / negativeCount;

			double averageGainAmount = positiveCount == 0 ? 0 : totalPositiveAmount / positiveCount;
			double averageLossAmount = negativeCount == 0 ? 0 : totalNegativeAmount / negativeCount;

			String side;
			if (returns == longReturns) {
				side = "";
				System.out.println("===[ " + (symbol == null ? "" : symbol) + " results using parameters: " + e.getKey()
						+ " ]===");
			} else {
				side = "short trades";
			}

			double pl = totalPositiveAmount + totalNegativeAmount;
			System.out.println("Negative " + side + ": " + negativeCount + " trades, avg. loss: "
					+ printAmountAndPercentage(averageLossAmount, averageLossPct));
			System.out.println("Positive " + side + ": " + positiveCount + " trades, avg. gain: "
					+ printAmountAndPercentage(averageGainAmount, averageGainPct));
			System.out
					.println("Returns  " + side + ": " + printAmountAndPercentage(pl, (pl / initialInvestment) * 100));

			if (returns == shortReturns) {
				if (symbol != null) {
					double buyAndHold = (initialInvestment / firstCandle.close) * lastCandle.close;
					pl = buyAndHold - initialInvestment;
					System.out.println("Buy&Hold: " + printAmountAndPercentage(pl, (pl / initialInvestment) * 100));
				}
			}
		}
	}

	private String printAmountAndPercentage(double amount, double percentage) {
		DecimalFormat formatter = CHANGE_FORMAT.get();
		String price = "$";
		if (trader == null) {
			price += amount;
		} else {
			price += trader.referencePriceDetails().priceToString(amount);
		}

		return price + " (" + formatter.format(percentage / 100) + ")";
	}

	public void simulationEnded(Trader trader, Client client) {
		printTradeStats();
	}
}
