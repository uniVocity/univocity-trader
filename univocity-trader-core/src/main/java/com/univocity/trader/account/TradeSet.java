package com.univocity.trader.account;

public final class TradeSet extends SortedSet<Trade> {

	public TradeSet() {
		super(new Trade[2]);
	}
}
