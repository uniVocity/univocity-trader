package com.univocity.trader.exchange.interactivebrokers;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import org.slf4j.*;

import java.util.concurrent.*;


class IBAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(IBAccount.class);

	private final IB ib;
	private final Account account;

	private final ConcurrentHashMap<String, Balance> balances = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, OrderBook> orderBooks = new ConcurrentHashMap<>();

	public IBAccount(IB ib, Account account) {
		this.ib = ib;
		this.account = account;
		ib.getAccountBalances(account.referenceCurrency(), balances);
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		return null;
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances(boolean force) {
		return balances;
	}

	public void resetBalances() {
		balances.clear();
		ib.resetAccountBalances();
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		OrderBook book = orderBooks.computeIfAbsent(symbol, s -> new OrderBook(this, symbol, depth));
		ib.getOrderBook(book, false, symbol, depth);
		return book;
	}

	public void closeOrderBook(String symbol) {
		ib.closeOrderBook(symbol);
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return null;
	}

	@Override
	public void cancel(Order order) {

	}
}
