package com.univocity.trader.notification;

import com.univocity.trader.account.*;

/**
 * A listener that receives any {@link Order} created in the exchange through
 * the framework, typically used to collect statistics and notify the user.
 */
public interface OrderListener {

	/**
	 * Notification of a new {@link Order} submission to the exchange. You must
	 * override {@link #orderFinalized(Order, Trade, Client)} to receive
	 * notifications of when orders are actually finalized (i.e. either
	 * {@code FILLED} or {@code CANCELLED})
	 *
	 * @param order  the order created in the exchange
	 * @param trade  the object responsible to track a trade from the first order
	 *               creation, and which contains many details regarding the current
	 *               symbol, such as {@link Trade#lastClosingPrice()} and
	 *               {@link Trade#latestCandle()}; and the trade itself, e.g.
	 *               {@link Trade#averagePrice()}, {@link Trade#minPrice()},
	 *               {@link Trade#ticks()}, etc. Might be {@code null}.
	 * @param client the client whose account was used to place the given order.
	 */
	default void orderSubmitted(Order order, Trade trade, Client client) {

	}

	/**
	 * Notification that an order already submitted to the exchange is finalized
	 * (i.e. either {@code FILLED} or {@code CANCELLED}). Notice that a cancelled
	 * order might have been partially filled to some point, so check if
	 * {@link Order#getFillPct()} is equal to {@code 0.0} to find out whether the
	 * order affected the account balance.
	 *
	 * @param order  the order created in the exchange
	 * @param trade  the object responsible to track a trade from the first order
	 *               creation, and which contains many details regarding the current
	 *               symbol, such as {@link Trade#lastClosingPrice()} and
	 *               {@link Trade#latestCandle()}; and the trade itself, e.g.
	 *               {@link Trade#averagePrice()}, {@link Trade#minPrice()},
	 *               {@link Trade#ticks()}, etc. Might be {@code null}.
	 * @param client the client whose account was used to place the given order.
	 */
	default void orderFinalized(Order order, Trade trade, Client client) {

	}

	/**
	 * Notification that a simulation has ended. Not used when trading live.
	 *
	 * @param trader the object responsible for the management of individual
	 *               {@link Trade} objects
	 * @param client the client whose simulated account was used to place the given
	 *               order.
	 */
	default void simulationEnded(Trader trader, Client client) {

	}

}
