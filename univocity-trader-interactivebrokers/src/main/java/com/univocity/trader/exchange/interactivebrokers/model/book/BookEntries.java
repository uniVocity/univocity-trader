package com.univocity.trader.exchange.interactivebrokers.model.book;

import java.util.*;

class BookEntries {
	private final Map<Integer, BookEntry> entries = new TreeMap<>();

	public void add(int position, String marketMaker, double price, int size) {
		entries.put(position, new BookEntry(marketMaker, price, size));
	}

	public void remove(int position) {
		entries.remove(position);
	}

	public BookEntry[] getEntries() {
		return entries.values().toArray(new BookEntry[0]);
	}

	public int size(){
		return entries.size();
	}
}