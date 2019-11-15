package com.univocity.trader.account;

import com.univocity.trader.*;

public class SimpleTradingFees implements TradingFees {

	private final double makerFeeAmount;
	private final double makerFeePercentage;
	private final double takerFeeAmount;
	private final double takerFeePercentage;

	public static SimpleTradingFees amount(double fee) {
		return amount(fee, fee);
	}

	public static SimpleTradingFees percentage(double fee) {
		return percentage(fee, fee);
	}

	public static SimpleTradingFees amount(double maker, double taker) {
		return new SimpleTradingFees(maker, 0.0, taker, 0.0);
	}

	public static SimpleTradingFees percentage(double maker, double taker) {
		return new SimpleTradingFees(0.0, maker, 0.0, taker);
	}

	protected SimpleTradingFees(double makerFeeAmount, double makerFeePercentage, double takerFeeAmount, double takerFeePercentage) {
		this.makerFeeAmount = makerFeeAmount;
		this.makerFeePercentage = makerFeePercentage;
		this.takerFeeAmount = takerFeeAmount;
		this.takerFeePercentage = takerFeePercentage;
	}

	@Override
	public double takeFee(double amount, Order.Type orderType, Order.Side orderSide) {
		switch (orderType) {
			case MARKET:
				return negativeToZero(takeFee(amount - takerFeeAmount, takerFeePercentage));
			case LIMIT:
				return negativeToZero(takeFee(amount - makerFeeAmount, makerFeePercentage));
		}
		throw new IllegalStateException("Unsupported Order.Type: " + orderType);
	}

	private double takeFee(double amount, double fee) {
		return amount * (1 - (fee / 100));
	}

	private double negativeToZero(double amount) {
		if (amount < 0) {
			return 0.0;
		}
		return amount;
	}

	@Override
	public String toString() {
		return "SimpleTradingFees{" +
				"makerFeeAmount=" + makerFeeAmount +
				", makerFeePercentage=" + makerFeePercentage +
				", takerFeeAmount=" + takerFeeAmount +
				", takerFeePercentage=" + takerFeePercentage +
				'}';
	}
}
