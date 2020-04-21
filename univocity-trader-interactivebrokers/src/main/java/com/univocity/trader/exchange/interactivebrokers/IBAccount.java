package com.univocity.trader.exchange.interactivebrokers;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.util.concurrent.*;


class IBAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(IBAccount.class);

	private final IB ib;
	private final Account account;

	public IBAccount(IB ib, Account account) {
		this.ib = ib;
		this.account = account;
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		return null;
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances() {
		return ib.getAccountBalances(account.referenceCurrency());
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return ib.getOrderBook(this, false, symbol, depth);
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return null;
	}

	@Override
	public void cancel(Order order) {

	}
}
