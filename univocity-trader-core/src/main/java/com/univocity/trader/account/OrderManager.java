package com.univocity.trader.account;


import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.strategy.*;

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
 * This happens in a separate thread that requests a new order stats using {@link AccountManager#updateOrderStatus(Order)}, at the rate specified by
 * {@link OrderManager#getOrderUpdateFrequency()}. Once the updated {@link Order} details are available, the order monitor thread will invoke
 * {@link #unchanged(Order)} if no changes happened to the order since the last status update, {@link #updated(Order)} if the executed quantity changed
 * (i.e. the order is being {@code PARTIALLY_FILLED}), or {@link #finalized(Order)} when the order was {@code FILLED} or {@code CANCELLED}.
 *
 * If the user does not specific an {@link OrderManager} implementation, the {@link DefaultOrderManager} will be used. Notice that this default implementation
 * will determine unit prices on each buy/sell order based on the central point of the gap in the {@link OrderBook}, and that orders which are not
 * {@code FILLED} within 10 minutes will be cancelled (via {@link Order#cancel()}).
 *
 * Note that an order might also be cancelled if it is not yet {@code FILLED} and another {@code BUY} or {@code SELL} signal appears for
 * another instrument when there are no funds available. In that case, {@link AccountManager#cancelStaleOrders()} will be used to cycle through
 * all pending orders, which will in turn invoke {@link #cancelToReleaseFundsFor(Order, Trader)}. The implementation of this method should decide whether or
 * not to execute {@link Order#cancel()} and cancel the order so that the instrument of the given {@link Trader} can be traded.
 */
public interface OrderManager {

	TimeInterval DEFAULT_ORDER_UPDATE_FREQUENCY = TimeInterval.seconds(10);

	void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle);

	default TimeInterval getOrderUpdateFrequency() {
		return DEFAULT_ORDER_UPDATE_FREQUENCY;
	}

	void finalized(Order order);

	void updated(Order order);

	void unchanged(Order order);

	void cancelToReleaseFundsFor(Order order, Trader newSymbolTrader);
}
