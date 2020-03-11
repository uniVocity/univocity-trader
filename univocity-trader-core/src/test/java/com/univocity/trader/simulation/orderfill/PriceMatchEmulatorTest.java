package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import org.junit.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Order.Type.*;
import static com.univocity.trader.simulation.orderfill.SlippageEmulatorTest.*;
import static org.junit.Assert.*;

public class PriceMatchEmulatorTest {

	PriceMatchEmulator e = new PriceMatchEmulator();

	@Test
	public void fillLimitSellOrders() {
		DefaultOrder sell = newOrder(LIMIT, SELL, 1.0, 1.0);

		e.fillOrder(sell, new Candle(1, 1, 0.9, 0.9, 0.9, 0.9, 1.0));
		assertEquals(NEW, sell.getStatus());

		e.fillOrder(sell, new Candle(1, 1, 1.1, 1.1, 1.1, 1.1, 1.0));
		assertEquals(FILLED, sell.getStatus());
		assertEquals(1.1, sell.getAveragePrice(), 0.000001);
	}

	@Test
	public void fillLimitBuyOrders(){
		DefaultOrder buy = newOrder(LIMIT, BUY, 1.0, 1.0);

		e.fillOrder(buy, new Candle(1, 1, 1.1, 1.1, 1.1, 1.1, 1.0));
		assertEquals(NEW, buy.getStatus());

		e.fillOrder(buy, new Candle(1, 1, 0.9, 0.9, 0.9, 0.9, 1.0));
		assertEquals(FILLED, buy.getStatus());
		assertEquals(0.9, buy.getAveragePrice(), 0.000001);
	}


	@Test
	public void fillMarketSellOrders() {
		DefaultOrder sell = newOrder(MARKET, SELL, 1.0, 1.0);

		e.fillOrder(sell, new Candle(1, 1, 0.9, 0.9, 0.9, 0.9, 1.0));
		assertEquals(FILLED, sell.getStatus());
		assertEquals(0.9, sell.getAveragePrice(), 0.000001);
	}

	@Test
	public void fillMarketBuyOrders(){
		DefaultOrder buy = newOrder(MARKET, BUY, 1.0, 1.0);

		e.fillOrder(buy, new Candle(1, 1, 1.1, 1.1, 1.1, 1.1, 1.0));
		assertEquals(FILLED, buy.getStatus());
		assertEquals(1.1, buy.getAveragePrice(), 0.000001);
	}
}