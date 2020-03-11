package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import org.junit.*;

import java.math.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Type.*;
import static org.junit.Assert.*;

public class SlippageEmulatorTest {

	private static final SlippageEmulator emulator = new SlippageEmulator();

	private void tryFill(DefaultOrder order, Candle candle) {
		emulator.fillOrder(order, candle);
	}

	static DefaultOrder newOrder(Order.Type type, Order.Side side, double price, double quantity) {
		DefaultOrder order = new DefaultOrder("BTC", "USDT", side, Trade.Side.LONG, System.currentTimeMillis());
		order.setType(type);

		order.setPrice(price);
		order.setStatus(Order.Status.NEW);
		order.setExecutedQuantity(0.0);
		order.setQuantity(quantity);
		return order;
	}

	@Test
	public void testFillLimitBuy() {
		DefaultOrder order = newOrder(LIMIT, BUY, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 100.5));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		//14 pips above order price, just one pip under or equal
		order = newOrder(LIMIT, BUY, 10_000, 1);
		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 2.0));
		assertEquals(0.25, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 2.0));
		assertEquals(0.5, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_990, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		order = newOrder(LIMIT, BUY, 10_000, 1);
		//volume zero = no change
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 10_000, 0.0));
		assertEquals(0.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.NEW, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(0, order.getAveragePrice(), 0.001);

		//price was never higher than what we want to pay, must fill
		tryFill(order, new Candle(1, 2, 9_999, 9_999, 9_999, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(9_999, order.getAveragePrice(), 0.001);

		order = newOrder(LIMIT, BUY, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_001, 10_001, 10_001, 10_001, 1.0));
		assertEquals(0.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.NEW, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(0, order.getAveragePrice(), 0.001);

		//price the same as what we want to pay, fills with traded volume
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 9_999, 0.3));
		assertEquals(0.3, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		//price the same as what we want to pay, must fill
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);
	}


	@Test
	public void testFillLimitSell() {
		DefaultOrder order = newOrder(LIMIT, SELL, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 100.5));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		//14 pips above order price, just one pip under or equal
		order = newOrder(LIMIT, SELL, 10_000, 1);
		tryFill(order, new Candle(1, 2, 10_000, 10_001, 9_985, 10_000, 2.0));
		assertEquals(0.25, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_001, 9_985, 10_000, 2.0));
		assertEquals(0.5, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_990, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		order = newOrder(LIMIT, SELL, 10_000, 1);
		//volume zero = no change
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 10_000, 0.0));
		assertEquals(0.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.NEW, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(0.0, order.getAveragePrice(), 0.001);

		//price was never higher than what we want to sell, must fill
		tryFill(order, new Candle(1, 2, 10_001, 10_001, 10_001, 10_001, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_001, order.getAveragePrice(), 0.001);


		order = newOrder(LIMIT, SELL, 10_000, 1);

		tryFill(order, new Candle(1, 2, 9_999, 9_999, 9_999, 9_999, 1.0));
		assertEquals(0.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.NEW, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(0.0, order.getAveragePrice(), 0.001);

		//price the same as what we want to sell, fills with traded volume
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 9_999, 0.3));
		assertEquals(0.3, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);

		//price the same as what we want to sell, must fill
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_000, order.getPrice(), 0.001);
		assertEquals(10_000, order.getAveragePrice(), 0.001);
	}

	@Test
	public void testFillMarketBuy() {
		DefaultOrder order = newOrder(MARKET, BUY, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 100.5));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_001.666, order.getPrice(), 0.001);

		//14 pips above order price, just one pip under or equal
		order = newOrder(MARKET, BUY, 10_000, 1);
		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_002.833, order.getPrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_002.833, order.getPrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_990, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_002.833, order.getPrice(), 0.001);

		order = newOrder(MARKET, BUY, 10_000, 1);
		//volume zero = emulates fill regardless, assumes 1.5% slippage
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 10_000, 0.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_001.5, order.getPrice(), 0.001);

		order = newOrder(MARKET, BUY, 10_000, 1);
		//price was never higher than what we want to pay, must fill
		tryFill(order, new Candle(1, 2, 9_999, 9_999, 9_999, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999, order.getPrice(), 0.001);


		order = newOrder(MARKET, BUY, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_001, 10_001, 10_001, 10_001, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_001.0, order.getPrice(), 0.001);

		order = newOrder(MARKET, BUY, 10_000, 1);
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 9_999, 9_999, 0.3));
		assertEquals(0.3, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(9_999.8889, order.getPrice(), 0.001);

		//price the same as what we want to pay, must fill
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999.8889, order.getPrice(), 0.001);
	}

	@Test
	public void testFillMarketSell() {
		DefaultOrder order = newOrder(MARKET, SELL, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 100.5));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999.8889, order.getPrice(), 0.001);

		//14 pips above order price, just one pip under or equal
		order = newOrder(MARKET, SELL, 10_000, 1);
		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999, order.getPrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_999, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999, order.getPrice(), 0.001);

		tryFill(order, new Candle(1, 2, 10_000, 10_015, 9_990, 10_000, 2.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999, order.getPrice(), 0.001);

		order = newOrder(MARKET, SELL, 10_000, 1);
		//volume zero = assumes 1.5% slippage
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 10_000, 10_000, 0.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_998.5, order.getPrice(), 0.001);

		order = newOrder(MARKET, SELL, 10_000, 1);
		//price was never higher than what we want to pay, must fill
		tryFill(order, new Candle(1, 2, 9_999, 9_999, 9_999, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999, order.getPrice(), 0.001);


		order = newOrder(MARKET, SELL, 10_000, 1);

		tryFill(order, new Candle(1, 2, 10_001, 10_001, 10_001, 10_001, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(10_001.0, order.getPrice(), 0.001);

		order = newOrder(MARKET, SELL, 10_000, 1);
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 9_999, 9_999, 0.3));
		assertEquals(0.3, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.PARTIALLY_FILLED, order.getStatus());
		assertEquals(9_999.7778, order.getPrice(), 0.001);

		//price the same as what we want to pay, must fill
		tryFill(order, new Candle(1, 2, 10_000, 10_000, 9_999, 9_999, 1.0));
		assertEquals(1.0, order.getExecutedQuantity(), 0.00001);
		assertEquals(Order.Status.FILLED, order.getStatus());
		assertEquals(9_999.7778, order.getPrice(), 0.001);
	}
}