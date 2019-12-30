package com.univocity.trader.account;


import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

import java.util.function.*;

/**
 * The {@code OrderManager} is responsible to preparing, submitting and tracking the state of an {@link Order}
 * submitted to the {@link Exchange} via {@link AccountManager#buy(String, String, double)} or {@link AccountManager#sell(String, String, double)}.
 *
 * For example, when the {@link com.univocity.trader.strategy.Strategy} produces a {@code BUY} or {@code SELL} {@link Signal},
 * the {@link Engine} will request the associated {@link Trader} of the observed instrument to perform the corresponding action (i.e. submitting
 * a buy or sell order). Based on the funds/assets available, the {@link AccountManager} will produce an initial {@link OrderRequest}, which will in
 * turn be sent to the {@code OrderManager} via {@link #prepareOrder(SymbolPriceDetails, OrderBook, OrderRequest, Candle)}.
 *
 * This callback method allows the user to modify the initial {@link OrderRequest}, i.e. adjusting the quantity to be bought/sold, unit price,
 * order type, etc.
 *
 * Once the {@link OrderRequest} is submitted to the exchange, an {@link Order} will be returned and tracked until it's {@code FILLED} or {@code CANCELLED}.
 * This happens in a separate thread that requests a new order status using {@link AccountManager#updateOrderStatus(Order)}, at the rate specified by
 * {@link OrderManager#getOrderUpdateFrequency()}. Once the updated {@link Order} details are available, the order monitor thread will invoke
 * {@link #unchanged(Order, Trader, Consumer)} if no changes happened to the order since the last status update,
 * {@link #updated(Order, Trader, Consumer)} if the executed quantity changed
 * (i.e. the order is being {@code PARTIALLY_FILLED}), or {@link #finalized(Order, Trader)} when the order was {@code FILLED} or {@code CANCELLED}.
 *
 * If the user does not specifies an {@link OrderManager} implementation, the {@link DefaultOrderManager} will be used. Notice that this default implementation
 * will determine unit prices on each buy/sell order based on the central point of the gap in the {@link OrderBook}. Also note that orders which are not
 * {@code FILLED} within 10 minutes will be cancelled (via {@link Order#cancel()}).
 *
 * An order might also be cancelled if it is not yet {@code FILLED} and another {@code BUY} or {@code SELL} signal appears for
 * another instrument when there are no funds available. In that case, {@link AccountManager#cancelStaleOrdersFor(Trader)} will be used to cycle through
 * all pending orders, which will in turn invoke {@link #cancelToReleaseFundsFor(Order, Trader, Trader)}. The implementation of this method should decide whether or
 * not to cancel the order so that the instrument of the given {@link Trader} can be traded.
 */
public interface OrderManager {

	/**
	 * The default time interval to wait between calls to {@link AccountManager#updateOrderStatus(Order)}, to identify if an {@link Order} has
	 * been {@code FILLED}, {@code CANCELLED} or {@code PARTIALLY_FILLED}.
	 */
	TimeInterval DEFAULT_ORDER_UPDATE_FREQUENCY = TimeInterval.seconds(30);

	/**
	 * Prepares a given {@link OrderRequest} for submission to the exchange (via {@link AccountManager#buy(String, String, double)} or
	 * {@link AccountManager#sell(String, String, double)}). Allows for modifications in the price and quantity pre-filled using the
	 * available allocated funds to the instrument represented by the symbol being traded. The order request can be cancelled via
	 * {@link OrderRequest#cancel()}
	 *
	 * @param priceDetails price details associated with the symbol of the given order request, which includes number of decimal digits to use
	 *                     and minimum order quantity. Note that after this method executes, the order price and amount will be adjusted to conform
	 *                     to the given price details. If no price details exist, this parameter will be set to {@code SymbolPriceDetails.NOOP}.
	 * @param book         a snapshot of the current state of the order book for the traded symbol. Use {@link OrderBook#update(int)} to receive
	 *                     a new snapshot.
	 * @param order        the order request to be adjusted if needed
	 * @param latestCandle the latest candle received from the exchange for the current order symbol (in {@link OrderRequest#getSymbol()}
	 */
	void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle);

	/**
	 * The time interval to wait between calls to {@link AccountManager#updateOrderStatus(Order)}, to identify if an {@link Order} has
	 * been {@code FILLED}, {@code CANCELLED} or {@code PARTIALLY_FILLED}.
	 *
	 * @return the desired frequency of requests for updates on any {@link Order} status
	 */
	default TimeInterval getOrderUpdateFrequency() {
		return DEFAULT_ORDER_UPDATE_FREQUENCY;
	}

	/**
	 * Notifies that an order is now {@code FILLED} or {@code CANCELLED}, and won't be monitored for updates anymore.
	 *
	 * @param order  the order that you probably won't have to care about anymore.
	 * @param trader the {@code Trader} responsible for the order
	 */
	void finalized(Order order, Trader trader);

	/**
	 * Notifies that an order has been updated, meaning it got {@code PARTIALLY_FILLED}.
	 *
	 * @param order        the order which got partially filled and has an updated amount in {@link Order#getExecutedQuantity()}
	 * @param trader       the {@code Trader} responsible for the order
	 * @param resubmission a consumer provided by the framework which allows requesting for
	 *                     the current order to be cancelled then resubmitted to the exchange, effectively
	 *                     invoking {@link #prepareOrder(SymbolPriceDetails, OrderBook, OrderRequest, Candle)} with
	 *                     new order details. The resubmission request will be ignored if the order is 98% filled or more
	 *                     (i.e. {@link Order#getFillPct()}  is greater than 98.0).
	 */
	void updated(Order order, Trader trader, Consumer<Order> resubmission);

	/**
	 * Notifies that an order has not been changed since the last status check, which happened at the time defined by
	 * {@link #getOrderUpdateFrequency()}.
	 *
	 * @param order        the order that's not being filled. Its status (in {@link Order#getStatus()}) should be
	 *                     either {@code NEW} or {@code PARTIALLY_FILLED}.
	 * @param trader       the {@code Trader} responsible for the order
	 * @param resubmission a consumer provided by the framework which allows requesting for
	 *                     the current order to be cancelled then resubmitted to the exchange, effectively
	 *                     invoking {@link #prepareOrder(SymbolPriceDetails, OrderBook, OrderRequest, Candle)} with
	 *                     new order details. The resubmission request will be ignored if the order is 98% filled or more
	 *                     (i.e. {@link Order#getFillPct()} is greater than 98.0).
	 */
	void unchanged(Order order, Trader trader, Consumer<Order> resubmission);

	/**
	 * Requests to cancel a given order that has not been {@code FILLED} to release funds for executing another order for another symbol.
	 * The details of the other symbol that cannot be traded due to lack of funds can be obtained from the {@code newSymbolTrader} object
	 *
	 * @param order           an order that has not been completely filled yet.
	 * @param orderTrader     the {@link Trader} of the current order that has not been filled.
	 * @param newSymbolTrader the {@link Trader} of the symbol that could not be traded due to lack of funds.
	 *
	 * @return {@code true} if the given order can be cancelled, otherwise {@code false}.
	 */
	boolean cancelToReleaseFundsFor(Order order, Trader orderTrader, Trader newSymbolTrader);

}
