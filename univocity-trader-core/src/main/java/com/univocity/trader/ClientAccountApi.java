package com.univocity.trader;

import com.univocity.trader.account.*;

import java.util.*;

public interface ClientAccountApi {

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
