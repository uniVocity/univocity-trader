package com.univocity.trader.notification;

import com.univocity.trader.account.*;

/**
 * A listener that receives any {@link Order} created in the exchange through the framework, typically used
 * to collect statistics and notify the user.
 */
public interface OrderListener {

	/**
	 * Notification of a new {@link Order}.
	 *
	 * @param order  the order created in the exchange
	 * @param trader the object responsible for the order creation, be it a {@code BUY} or {@code SELL}, and which contains many details
	 *               regarding the current symbol, such as {@link Trader#getLastClosingPrice()} and {@link Trader#getCandle()};
	 *               and the trade itself, e.g. {@link Trader#getBoughtPrice()}, {@link Trader#getMinPrice()}, {@link Trader#getTicks()}, etc.
	 * @param client the client whose account was used to place the given order.
	 */
	void onOrder(Order order, Trader trader, Client client);

	/**
	 * Notification that a simulation has ended. Not used when trading live.
	 *
	 * @param trader the object responsible for the order creation, be it a {@code BUY} or {@code SELL}, and which contains many details
	 *               regarding the current symbol, such as {@link Trader#getLastClosingPrice()} and {@link Trader#getCandle()};
	 *               and the trade itself, e.g. {@link Trader#getBoughtPrice()}, {@link Trader#getMinPrice()}, {@link Trader#getTicks()}, etc.
	 * @param client the client whose simulated account was used to place the given order.
	 */
	default void onSimulationEnd(Trader trader, Client client) {

	}

}
