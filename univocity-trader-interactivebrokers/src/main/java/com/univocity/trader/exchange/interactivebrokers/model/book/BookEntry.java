package com.univocity.trader.exchange.interactivebrokers.model.book;

public class BookEntry {
	public final String marketMaker;
	public final double price;
	public final int quantity;

	BookEntry(String marketMaker, double price, int quantity) {
		this.marketMaker = marketMaker;
		this.price = price;
		this.quantity = quantity;
	}
}