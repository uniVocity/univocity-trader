package com.univocity.trader.notification;

import com.univocity.trader.account.*;

/**
 * A listener that receives any {@link Order} created in the exchange through the framework, typically used
 * to collect statistics and notify the user.
 */
public interface OrderListener {

	/**
	 * Notification of a new {@link Order} submission to the exchange. You must override {@link #orderFinalized(Order, Trader, Client)} to
	 * receive notifications of when orders are actually finalized (i.e. either {@code FILLED} or {@code CANCELLED})
	 *
	 * @param order  the order created in the exchange
	 * @param trader the object responsible for the order creation, be it a {@code BUY} or {@code SELL}, and which contains many details
	 *               regarding the current symbol, such as {@link Trader#lastClosingPrice()} and {@link Trader#latestCandle()};
	 *               and the trade itself, e.g. {@link Trader#averagePrice()}, {@link Trader#minPrice()}, {@link Trader#ticks()}, etc.
	 * @param client the client whose account was used to place the given order.
	 */
	void orderSubmitted(Order order, Trader trader, Client client);

	/**
	 * Notification that an order already submitted to the exchange is finalized (i.e. either {@code FILLED} or {@code CANCELLED})
	 *
	 * @param order  the order created in the exchange
	 * @param trader the object responsible for the order creation, be it a {@code BUY} or {@code SELL}, and which contains many details
	 *               regarding the current symbol, such as {@link Trader#lastClosingPrice()} and {@link Trader#latestCandle()};
	 *               and the trade itself, e.g. {@link Trader#averagePrice()}, {@link Trader#minPrice()}, {@link Trader#ticks()}, etc.
	 * @param client the client whose account was used to place the given order.
	 */
	default void orderFinalized(Order order, Trader trader, Client client) {

	}

	/**
	 * Notification that a simulation has ended. Not used when trading live.
	 *
	 * @param trader the object responsible for the order creation, be it a {@code BUY} or {@code SELL}, and which contains many details
	 *               regarding the current symbol, such as {@link Trader#lastClosingPrice()} and {@link Trader#latestCandle()};
	 *               and the trade itself, e.g. {@link Trader#averagePrice()}, {@link Trader#minPrice()}, {@link Trader#ticks()}, etc.
	 * @param client the client whose simulated account was used to place the given order.
	 */
	default void simulationEnded(Trader trader, Client client) {

	}

}
