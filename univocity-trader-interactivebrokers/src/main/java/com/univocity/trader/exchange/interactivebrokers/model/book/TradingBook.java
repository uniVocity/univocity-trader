package com.univocity.trader.exchange.interactivebrokers.model.book;

import com.ib.client.*;

public class TradingBook {
	private static final int INSERT = 0;
	private static final int UPDATE = 1;
	private static final int DELETE = 2;
	private static final int BID = 1;

	private final BookEntries bids = new BookEntries();
	private final BookEntries asks = new BookEntries();
	private final int id;
	private final int depth;
	private final boolean isSmartDepth;

	public TradingBook(int id, int depth, boolean isSmartDepth) {
		this.isSmartDepth = isSmartDepth;
		this.id = id;
		this.depth = depth;
	}

	public void updateBook(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
		BookEntries book = side == BID ? bids : asks;
		if (operation == INSERT || operation == UPDATE) {
			book.add(position, marketMaker, price, size);
		} else if (operation == DELETE) {
			book.remove(position);
		}
	}

	public boolean isReady(){
		 return bids.size() >= depth && asks.size() >= depth;
	}

	public BookEntry[] bids() {
		return bids.getEntries();
	}

	public BookEntry[] asks() {
		return asks.getEntries();
	}

	public void stopWatching(EClient client) {
		client.cancelMktDepth(id, isSmartDepth);
	}
}


