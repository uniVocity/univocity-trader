package com.univocity.trader.account;


import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;

public interface OrderManager {

	TimeInterval DEFAULT_ORDER_UPDATE_FREQUENCY = TimeInterval.seconds(10);

	void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle);

	default TimeInterval getOrderUpdateFrequency(){
		return DEFAULT_ORDER_UPDATE_FREQUENCY;
	}

	void finalized(Order order);

	void updated(Order order);

	void unchanged(Order order);

	void cancelToReleaseFunds(Order order);
}
