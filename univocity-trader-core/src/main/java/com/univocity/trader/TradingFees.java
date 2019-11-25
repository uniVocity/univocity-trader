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
