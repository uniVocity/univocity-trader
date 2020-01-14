package com.univocity.trader.exchange.interactivebrokers;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.util.*;


class InteractiveBrokersAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(InteractiveBrokersAccount.class);

	private final IB ib;

	public InteractiveBrokersAccount(IB ib) {
		this.ib = ib;
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		return null;
	}

	@Override
	public Map<String, Balance> updateBalances() {
		return null;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return null;
	}

	@Override
	public void cancel(Order order) {

	}
}
