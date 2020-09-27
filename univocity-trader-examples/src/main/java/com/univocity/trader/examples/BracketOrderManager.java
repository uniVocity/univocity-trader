package com.univocity.trader.examples;

import com.univocity.trader.account.*;

import java.util.function.*;

public class BracketOrderManager implements OrderManager {

	@Override
	public void prepareOrder(OrderBook book, OrderRequest order, Context context) {

	}

	@Override
	public void finalized(Order order, Trader trader) {

	}

	@Override
	public void updated(Order order, Trader trader, Consumer<Order> resubmission) {

	}

	@Override
	public void unchanged(Order order, Trader trader, Consumer<Order> resubmission) {

	}

	@Override
	public boolean cancelToReleaseFundsFor(Order order, Trader orderTrader, Trader newSymbolTrader) {
		return false;
	}
}
