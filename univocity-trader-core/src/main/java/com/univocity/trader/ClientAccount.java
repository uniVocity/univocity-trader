package com.univocity.trader;

import com.univocity.trader.account.*;

import java.util.*;

/**
 * A {@code ClientAccount} implementation controls the account of a user on an exchange or broker,
 * and allows orders to be executed and monitored on behalf of the account holder. Ideally a {@code ClientAccount}
 * instance comes from {@link Exchange#connectToAccount(String, String)}.
 *
 * How a order is processed and tracked depends on an {@link OrderManager}, which receives updates on the order status
 * at frequent intervals and might allow cancellation of the order if it is not filled in a timely manner.
 *
 *
 * @see OrderManager
 * @see Exchange
 * @see Order
 * @see OrderRequest
 * @see OrderBook
 * @see TradingFees
 */
public interface ClientAccount {

	/**
	 * Submits an {@link OrderRequest} to the exchange. If it is not filled immediately, the {@link AccountManager} will
	 * keep track of its status until it is filled or cancelled. The {@link OrderManager} associated with the symbol
	 * being traded will be used to notify the user of updates (or lack thereof) on their order while it is still open:
	 *
	 * The order status will be updated by polling the state at a fixed interval defined by {@link OrderManager#getOrderUpdateFrequency).
	 *
	 * Once the order reaches status {@code FILLED} or {@code CANCELLED}, {@link OrderManager#finalized(Order)} will be called
	 * so its implementation can perform any cleanup necessary.
	 *
	 * If an order is updated (i.e. it is still open and some quantity was filled), {@link OrderManager#updated(Order)} will be called.
	 *
	 * If no changes occurred after polling the exchange for an update on the status of the order, {@link OrderManager#unchanged(Order)}
	 * will be called.
	 *
	 * @param orderDetails details of the order: buy/sell quantities of some instrument for a given unit price
	 *
	 * @return an {@link Order} with the current status of the order: e.g. filled, executed quantity and price, etc.
	 */
	Order executeOrder(OrderRequest orderDetails);

	Map<String, Balance> updateBalances();

	default TradingFees getTradingFees() {
		//default to 0 fees for live exchange implementation - the exchanges do that "service" for us.
		return SimpleTradingFees.percentage(0.0);
	}


	OrderBook getOrderBook(String symbol, int depth);

	Order updateOrderStatus(Order order);

	void cancel(Order order);
}
