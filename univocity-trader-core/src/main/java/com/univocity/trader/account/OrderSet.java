package com.univocity.trader.account;

public final class OrderSet extends SortedSet<Order> {

	public OrderSet() {
		super(new Order[4]);
	}
}
