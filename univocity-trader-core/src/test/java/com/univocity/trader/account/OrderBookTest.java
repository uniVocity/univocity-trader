package com.univocity.trader.account;

import org.junit.*;

import static org.junit.Assert.*;

public class OrderBookTest {

	private OrderBook book = getBook();

	public OrderBook getBook() {
		OrderBook out = new OrderBook(null, null, 10);

		//note: the order here doesn't matter, the values are sorted correctly
		out.addAsk(4.217, 647.454);
		out.addAsk(4.216, 404.754);
		out.addAsk(4.215, 133.366);
		out.addAsk(4.214, 594.148);
		out.addAsk(4.212, 56.587);
		out.addAsk(4.211, 219.341);
		out.addAsk(4.210, 277.476);
		out.addAsk(4.209, 149.821);

		out.addBid(4.203, 357.676);
		out.addBid(4.202, 1204.564);
		out.addBid(4.201, 150.415);
		out.addBid(4.199, 306.600);
		out.addBid(4.198, 546.000);
		out.addBid(4.197, 96.400);
		out.addBid(4.195, 1073.410);
		out.addBid(4.194, 700.00);

		return out;
	}

	@Test
	public void testAverageAskAmountDepthBased() {
		double price;

		price = book.getAverageAskAmount(1);
		assertEquals(4.209, price, 0.0001);

		price = book.getAverageAskAmount(2);
		assertEquals(/*4.209649375024865*/(4.209 * 149.821 + 4.210 * 277.476) / (149.821 + 277.476), price, 0.0001);
	}

	@Test
	public void testAverageBidAmountDepthBased() {
		double price;

		price = book.getAverageBidAmount(1);
		assertEquals(4.203, price, 0.0001);

		price = book.getAverageBidAmount(2);
		assertEquals(/*4.202228950737403*/(4.203 * 357.676 + 4.202 * 1204.564) / (357.676 + 1204.564), price, 0.0001);
	}

	@Test
	public void testSpreadAmountDepthBased() {
		double spread;

		spread = book.getSpread(1);
		assertEquals(0.006, spread, 0.0001);

		spread = book.getSpread(2);
		assertEquals(/*0.007420424287461813*/ 4.209649375024865 - 4.202228950737403, spread, 0.0001);
	}


	@Test
	public void testAverageAskAmountQuantityBased() {
		double price;

		price = book.getAverageAskAmount(50.0);
		assertEquals(4.209, price, 0.0001);

		price = book.getAverageAskAmount(500.0);
		assertEquals(/*4.209845764*/(4.209 * 149.821 + 4.210 * 277.476 + 4.211 * (500 - (149.821 + 277.476))) / 500.0, price, 0.0001);
	}

	@Test
	public void testAverageBidAmountQuantityBased() {
		double price;

		price = book.getAverageBidAmount(50.0);
		assertEquals(4.203, price, 0.0001);

		price = book.getAverageBidAmount(2000.0);
		assertEquals(/*4.2016726129999995*/(0
				+ 4.203 * 357.676
				+ 4.202 * 1204.564
				+ 4.201 * 150.415
				+ 4.199 * (2000 - (357.676 + 1204.564 + 150.415))
		) / 2000.0, price, 0.0001);
	}

	@Test
	public void testSpreadAmountQuantityBased() {
		double spread;

		spread = book.getSpread(50.0);
		assertEquals(0.006, spread, 0.0001);

		spread = book.getSpread(2000.0);
		assertEquals(0.011730481500000778, spread, 0.0001);
	}

}

