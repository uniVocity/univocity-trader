package com.univocity.trader;

import com.univocity.trader.account.*;

/**
 * Calculates the trading fees applied to an {@link Order}. Used for simulations/backtesting as the live {@link Exchange}
 * usually applies their fees for you. The purpose of providing a custom {@code TradingFees} implementation is to mimic
 * what would happen to your account balance when trading live. The closest to reality the fees are, the more accurate the
 * simulation will be.
 */
public interface TradingFees {

	/**
	 * Applies trading fees to a given order amount.
	 *
	 * @param amount    the original amount before fees
	 * @param orderType the type of order (i.e. {@code MARKET}, {@code LIMIT})
	 * @param side      the order side (i.e. {@code BUY}, {@code SELL}
	 *
	 * @return the amount invested after fees are taken.
	 */
	double takeFee(double amount, Order.Type orderType, Order.Side side);

	/**
	 * Return the trading fee amount applied to a given {@link Order}.
	 *
	 * @param order the order whose fees will be calculated
	 *
	 * @return the total fee amount for the given order;
	 */
	default double feesOnOrder(Order order) {
		final double amount = order.getTotalOrderAmount();
		return feesOnAmount(amount, order.getType(), order.getSide());
	}

	/**
	 * Return the trading fee amount applied to a given {@link OrderRequest}.
	 *
	 * @param order the order whose fees will be calculated
	 *
	 * @return the total fee amount for the given order;
	 */
	default double feesOnOrder(OrderRequest order) {
		final double amount = order.getTotalOrderAmount();
		return feesOnAmount(amount, order.getType(), order.getSide());
	}


	/**
	 * Return the trading fee amount applied to the actual amount spent on the given {@link Order}.
	 *
	 * @param order the order whose fees will be calculated
	 *
	 * @return the total fee amount for the value traded through this order.
	 */
	default double feesOnTradedAmount(Order order) {
		final double amount = order.getTotalTraded();
		if (amount == 0.0) {
			return 0.0;
		}
		return feesOnAmount(amount, order.getType(), order.getSide());
	}

	/**
	 * Return the trading fee amount applied to the partial fill amount spent on the given {@link Order}.
	 *
	 * @param order the order whose fees will be calculated
	 *
	 * @return the fee amount for the current partial fill value traded through this order.
	 */
	default double feesOnPartialFill(DefaultOrder order) {
		final double amount = order.getPartialFillTotalPrice();
		if (amount == 0.0) {
			return 0.0;
		}
		return feesOnAmount(amount, order.getType(), order.getSide());
	}

	/**
	 * Return the trading fee amount applied to a given total order amount.
	 *
	 * @param amount    the original amount before fees
	 * @param orderType the type of order (i.e. {@code MARKET}, {@code LIMIT})
	 * @param side      the order side (i.e. {@code BUY}, {@code SELL}
	 *
	 * @return the total fee amount for the given order amount;
	 */
	default double feesOnAmount(final double amount, Order.Type orderType, Order.Side side) {
		return amount - takeFee(amount, orderType, side);
	}


	/**
	 * Return the maximum trading fee amount that could be applied over the total order amount
	 * (not to be confused with actual the traded/filled amount).
	 *
	 * @param order the original order
	 *
	 * @return the total fee amount for the given order;
	 */
	default double feesOnTotalOrderAmount(Order order) {
		double amount = order.getTotalOrderAmount();
		return feesOnAmount(amount, order.getType(), order.getSide());
	}

	/**
	 * Returns the break even amount. Used to know how much the price has to move until the invested amount/quantity after fees becomes positive.
	 *
	 * @param amount the original amount spent on a trade (before fees).
	 *
	 * @return the minimum price change (usually in currency) required to recoup the trading fee costs.
	 */
	default double getBreakEvenAmount(double amount) {
		double out = takeFee(amount, Order.Type.LIMIT, Order.Side.BUY);
		out = takeFee(out, Order.Type.LIMIT, Order.Side.BUY);
		return amount + (amount - out);
	}

	/**
	 * Returns the break even percentage. Used to know how much the price has to move until the invested amount/quantity after fees becomes positive.
	 *
	 * @param amount the original amount spent on a trade (before fees).
	 *
	 * @return the minimum percentage change in price required to recoup the trading fee costs, in a scale from {@code 0.0} to {@code 100.0}.
	 */
	default double getBreakEvenChange(double amount) {
		if (amount == 0.0) {
			return 0.0;
		}
		double breakEvenAmount = getBreakEvenAmount(amount);
		return ((breakEvenAmount / amount) - 1) * 100.0;
	}

}
