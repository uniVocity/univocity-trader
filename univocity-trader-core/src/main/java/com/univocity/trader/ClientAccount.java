package com.univocity.trader;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.util.*;

/**
 * A {@code ClientAccount} implementation controls the account of a user on an exchange or broker,
 * and allows orders to be executed and monitored on behalf of the account holder. Ideally a {@code ClientAccount}
 * instance comes from {@link Exchange#connectToAccount(String, String)}.
 *
 * How a order is processed and tracked depends on an {@link OrderManager}, which receives updates on the order status
 * at frequent intervals and might allow cancellation of the order if it is not filled in a timely manner.
 *
 * The {@link DefaultOrderManager} will be used to handle all orders on all symbols unless the user associates
 * their own implementation to all or some symbols using {@link AccountConfiguration#setOrderManager(OrderManager, String...)}.
 *
 * Note that the {@link DefaultOrderManager} will keep orders open for up to 10 minutes by default, and will cancel the order
 * if it could be filled in full after that time. If the order is cancelled, the account will still retain the amount
 * purchased/sold if the order was {@code PARTIALLY_FILLED}.
 *
 * @see OrderManager
 * @see Exchange
 * @see AccountConfiguration
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
	 * An {@link Order} can be cancelled at any time after from the {@link OrderManager} by invoking {@link Order#cancel()}
	 *
	 * @param orderDetails details of the order: buy/sell quantities of some instrument for a given unit price
	 *
	 * @return an {@link Order} with the current status of the order: e.g. filled, executed quantity and price, etc.
	 */
	Order executeOrder(OrderRequest orderDetails);

	/**
	 * Updates the balances of all symbols traded by this account and returns them in a map of symbol to {@link Balance} instances, which
	 * includes free amounts and amounts locked in one or more {@link Order}s.
	 *
	 * @return a map of symbols traded by the account and the corresponding {@link Balance} of each one.
	 */
	Map<String, Balance> updateBalances();

	/**
	 * Returns the {@link TradingFees} applied to this account as determined by the exchange. Defaults to {@code 0.0} as the exchange generally
	 * subtracts the amount from the account directly. Test implementations of {@code ClientAccount} might (should!) want to
	 * return {@link TradingFees} that would calculate how much a simulated trade would pay in fees.
	 *
	 * @return A {@link TradingFees} implementation that applies the correct fees on every trade made by this
	 * account based on the fee structure used by the live exchange.
	 */
	default TradingFees getTradingFees() {
		//default to 0 fees for live exchange implementation - the exchanges do that "service" for us.
		return SimpleTradingFees.percentage(0.0);
	}


	/**
	 * Returns the latest state of {@link OrderBook} for a given symbol, with a given depth.
	 * Used by {@link OrderManager#prepareOrder(SymbolPriceDetails, OrderBook, OrderRequest, Candle)} to update the unit price and quantity
	 * to buy/sell in an initial {@link OrderRequest} which is always generated based on the latest close price of the symbol, and the current
	 * account free balance.
	 *
	 * Called whenever {@link AccountManager#buy(String, String, double)} or {@link AccountManager#sell(String, String, double)} is invoked, then
	 * the returned {@link OrderBook} will be sent for the {@link OrderManager} associated with the given symbol to prepare an order for execution.
	 *
	 * Note that unless the user provides their own {@link OrderManager},
	 * the {@link DefaultOrderManager} will use the {@link OrderBook} to calculate the central price point in the spread between
	 * bids and asks based on the order size and the ask/bid quantities and prices.
	 *
	 * @param symbol the symbol whose order book will be returned.
	 * @param depth  the depth of the order book, i.e. how many entries with bid/ask price should be returned.
	 *
	 * @return the most up-to-date snapshot of the order book for the given symbol.
	 */
	OrderBook getOrderBook(String symbol, int depth);

	/**
	 * Updates the status of a given {@link Order}. Used by the {@link AccountManager} to poll the exchange at the interval specified
	 * by {@link OrderManager#getOrderUpdateFrequency) untill the order is {@code CANCELLED} or {@code FILLED}.
	 *
	 * @param order the order whose status needs to be updated.
	 *
	 * @return the updated order to be sent to the {@link OrderManager} via {@link OrderManager#unchanged(Order)}, {@link OrderManager#updated(Order)} or
	 * {@link OrderManager#finalized(Order)} depending on the updated status of the order.
	 */
	Order updateOrderStatus(Order order);

	/**
	 * Cancels a given {@link Order} if it has not been {@code FILLED} yet.
	 *
	 * @param order the order to be cancelled.
	 */
	void cancel(Order order);
}
