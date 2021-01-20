package com.univocity.trader.account;

import org.junit.*;

import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Order.Type.*;
import static com.univocity.trader.account.Trade.Side.*;
import static com.univocity.trader.indicators.Signal.*;
import static junit.framework.TestCase.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class LongTradingTests extends OrderFillChecker {

	@Test
	public void testLongPositionTradingWithFullAccount() {
		SimulatedAccountManager account = getSimulatedAccountManager();

		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);

		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, 100, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 10, 0.8, BUY);
		double quantity2 = checkTradeAfterLongBuy(usdBalance, trade, 100, quantity1, 0.8, 1.1, 0.8);

		double averagePrice = (addFees(quantity1 * 1.0) + addFees(quantity2 * 0.8)) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), DELTA);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 20, 0.95, SELL);
		checkTradeAfterLongSell(usdBalance, trade, (quantity1 + quantity2), 0.95, 1.1, 0.8);
		assertEquals(averagePrice, trade.averagePrice(), DELTA); //average price is about 0.889

		assertFalse(trade.stopped());
		assertEquals("Sell signal", trade.exitReason());
		assertFalse(trade.tryingToExit());

		checkProfitLoss(trade, initialBalance, (quantity1 * 1.0) + (quantity2 * 0.8));
	}

	@Test
	public void testLongPositionTrading() {
		SimulatedAccountManager account = getSimulatedAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration.maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 10, 0.8, BUY);
		double quantity2 = checkTradeAfterLongBuy(usdBalance, trade, MAX, quantity1, 0.8, 1.1, 0.8);

		double averagePrice = (addFees(quantity1 * 1.0) + addFees(quantity2 * 0.8)) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), DELTA);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 20, 0.95, SELL);
		checkTradeAfterLongSell(usdBalance, trade, (quantity1 + quantity2), 0.95, 1.1, 0.8);
		assertEquals(averagePrice, trade.averagePrice(), DELTA); //average price is about 0.889

		assertFalse(trade.stopped());
		assertEquals("Sell signal", trade.exitReason());
		assertFalse(trade.tryingToExit());

		checkProfitLoss(trade, initialBalance, (quantity1 * 1.0) + (quantity2 * 0.8));
	}

	@Test
	public void testTradingWithStopLoss() {
		SimulatedAccountManager account = getSimulatedAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration.maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");

		OrderRequest or = new OrderRequest("ADA", "USDT", Order.Side.SELL, LONG, 2, null);
		or.setQuantity(quantity1);
		or.setTriggerCondition(Order.TriggerCondition.STOP_LOSS, 0.9);
		Order o = executeOrder(trader, or);

		updateOpenOrders(trader, newTick(3, 1.5));
		assertEquals(Order.Status.NEW, o.getStatus());
		assertFalse(o.isActive());
		assertEquals(usdBalance, account.getAmount("USDT"), DELTA);

		updateOpenOrders(trader, newTick(4, 0.8999));
		assertEquals(Order.Status.NEW, o.getStatus());
		assertTrue(o.isActive());
		assertEquals(usdBalance, account.getAmount("USDT"), DELTA);

		updateOpenOrders(trader, newTick(4, 0.92));
		assertEquals(FILLED, o.getStatus());
		assertTrue(o.isActive());
		assertEquals(0.0, account.getAmount("ADA"), DELTA);
		assertEquals(usdBalance + ((o.getExecutedQuantity() /*quantity*/) * 0.92 /*price*/) * 0.999 /*fees*/, account.getAmount("USDT"), DELTA);
	}

	@Test
	public void testTradingWithStopGain() {
		SimulatedAccountManager account = getSimulatedAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration.maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		double cost = addFees(quantity1);

		usdBalance = account.getAmount("USDT");
		assertEquals(initialBalance - cost, usdBalance, DELTA);
		assertEquals(60.004039996, usdBalance, DELTA);
		assertEquals(quantity1, account.getAmount("ADA"), DELTA);

		OrderRequest or = new OrderRequest("ADA", "USDT", Order.Side.BUY, LONG, 2, null);
		or.setQuantity(quantity1);
		or.setTriggerCondition(Order.TriggerCondition.STOP_GAIN, 1.2);
		Order o = executeOrder(trader, or);

		updateOpenOrders(trader, newTick(3, 0.8999));
		assertEquals(Order.Status.NEW, o.getStatus());
		assertFalse(o.isActive());
		assertEquals(usdBalance - (addFees(o.getTotalOrderAmount())), account.getAmount("USDT"), DELTA);

		updateOpenOrders(trader, newTick(4, 1.5));
		assertTrue(o.isActive());
		assertEquals(Order.Status.NEW, o.getStatus()); //can't fill because price is too high and we want to pay 1.2
		assertEquals(usdBalance - addFees(o.getTotalOrderAmount()), account.getAmount("USDT"), DELTA);


		double previousUsdBalance = usdBalance;
		updateOpenOrders(trader, newTick(5, 0.8));
		assertTrue(o.isActive());
		assertEquals(FILLED, o.getStatus());

		assertEquals(o.getExecutedQuantity() + quantity1, account.getAmount("ADA"), DELTA);

		double actualFees = feesOn(o.getTotalTraded());
		assertEquals(previousUsdBalance - (o.getTotalTraded() + actualFees), account.getAmount("USDT"), DELTA);
	}

	@Test
	public void testLongTradingWithMarketBracketOrder() {
		SimulatedAccountManager account = getSimulatedAccountManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
				if (order.isBuy() && order.isLong() || order.isSell() && order.isShort()) {
					OrderRequest marketSellOnLoss = order.attachToPercentageChange(MARKET, -1.0);
					OrderRequest takeProfit = order.attachToPercentageChange(MARKET, 1.0);
				}
			}
		});


		final double MAX = 40.0;
		double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration.maximumInvestmentAmountPerTrade(MAX);

		initialBalance = testLongMarketBracketOrder(account, initialBalance, 1.0, -0.1, 10);
		initialBalance = testLongMarketBracketOrder(account, initialBalance, 1.0, -0.1, 20);
		initialBalance = testLongMarketBracketOrder(account, initialBalance, 1.0, 0.1, 30);
		initialBalance = testLongMarketBracketOrder(account, initialBalance, 1.0, 0.1, 40);

	}

	double testLongMarketBracketOrder(SimulatedAccountManager account, double initialBalance, double unitPrice, double priceIncrement, long time) {
		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, ++time, unitPrice, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBracketOrder(usdBalance, trade, 40.0, 0.0, unitPrice, unitPrice, unitPrice);
		usdBalance = account.getAmount("USDT");

		assertEquals(40.0 / unitPrice * 0.9999 * 0.999, quantity1); //40 minus offset + 2x fees
		assertEquals(initialBalance - (addFees(quantity1 * unitPrice)), usdBalance, DELTA); //attached orders submitted, so 1x fees again

		Order parent = trade.position().iterator().next();
		assertEquals(2, parent.getAttachments().size());

		Order profitOrder = null;
		Order lossOrder = null;

		for (Order o : parent.getAttachments()) {
			assertEquals(NEW, o.getStatus());
			assertEquals(parent.getOrderId(), o.getParentOrderId());
			assertFalse(o.isActive());
			if (o.getTriggerPrice() > unitPrice) {
				profitOrder = o;
			} else {
				lossOrder = o;
			}
		}

		assertNotNull(profitOrder);
		assertNotNull(lossOrder);

		assertEquals(parent, profitOrder.getParent());
		assertEquals(parent, lossOrder.getParent());

		unitPrice = unitPrice + priceIncrement;

		updateOpenOrders(trader, newTick(++time, unitPrice)); //this finalizes all orders
		updateOpenOrders(trader, newTick(++time, unitPrice)); //so this should not do anything

		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getFree(), DELTA);


		double currentBalance = account.getAmount("USDT");
		assertEquals(usdBalance + (quantity1 * unitPrice) * 0.999, currentBalance, DELTA);

		if (priceIncrement > 0) {
			assertEquals(CANCELLED, lossOrder.getStatus());
			assertEquals(FILLED, profitOrder.getStatus());
		} else {
			assertEquals(FILLED, lossOrder.getStatus());
			assertEquals(CANCELLED, profitOrder.getStatus());
		}


		return currentBalance;
	}

	@Test
	public void testLongTradingWithLimitBracketOrder() {
		SimulatedAccountManager account = getSimulatedAccountManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
				if (order.isBuy() && order.isLong() || order.isSell() && order.isShort()) {
					OrderRequest limitSellOnLoss = order.attachToPercentageChange(LIMIT, -1.0);
					OrderRequest takeProfit = order.attachToPercentageChange(LIMIT, 1.0);
				}
			}
		});


		final double MAX = 40.0;
		double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration.maximumInvestmentAmountPerTrade(MAX);

		initialBalance = testLongLimitBracketOrder(account, initialBalance, 1.0, -0.1, 10);
		initialBalance = testLongLimitBracketOrder(account, initialBalance, 1.0, -0.1, 20);
		initialBalance = testLongLimitBracketOrder(account, initialBalance, 1.0, 0.01, 30);
		initialBalance = testLongLimitBracketOrder(account, initialBalance, 1.0, 0.01, 40);

	}

	double testLongLimitBracketOrder(SimulatedAccountManager account, double initialBalance, double unitPrice, double priceIncrement, long time) {
		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, ++time, unitPrice, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBracketOrder(usdBalance, trade, 40.0, 0.0, unitPrice, unitPrice, unitPrice);
		usdBalance = account.getAmount("USDT");

		double amountSpent = addFees(quantity1 * unitPrice);

		assertEquals(40.0 / unitPrice * 0.9999 * 0.999, quantity1); //taking offset & fees out
		assertEquals(initialBalance - amountSpent, usdBalance, DELTA); //attached orders submitted, so 1x fees again

		Order parent = trade.position().iterator().next();
		assertEquals(2, parent.getAttachments().size());

		Order profitOrder = null;
		Order lossOrder = null;

		for (Order o : parent.getAttachments()) {
			assertEquals(NEW, o.getStatus());
			assertEquals(parent.getOrderId(), o.getParentOrderId());
			assertFalse(o.isActive());
			if (o.getTriggerPrice() > unitPrice) {
				profitOrder = o;
			} else {
				lossOrder = o;
			}
		}

		assertNotNull(profitOrder);
		assertNotNull(lossOrder);

		assertEquals(parent, profitOrder.getParent());
		assertEquals(parent, lossOrder.getParent());

		updateOpenOrders(trader, newTick(++time, unitPrice + priceIncrement)); //price increment goes way beyond limit

		double currentBalance = account.getAmount("USDT");

		assertEquals(0, account.getBalance("ADA").getFree(), DELTA);
		if (priceIncrement < 0) {
			assertEquals(quantity1, account.getBalance("ADA").getLocked(), DELTA);
			assertEquals(initialBalance - amountSpent, currentBalance, DELTA);

			assertTrue(lossOrder.isActive());
			assertFalse(profitOrder.isActive());

			unitPrice = unitPrice * 0.995;  //decrease 0.5% to allow limit order to fill

			updateOpenOrders(trader, newTick(++time, unitPrice)); //price increment is now in range.

			assertEquals(FILLED, lossOrder.getStatus());
			assertEquals(CANCELLED, profitOrder.getStatus());

			double amountSold = quantity1 * unitPrice * 0.999;

			currentBalance = account.getAmount("USDT");
			assertEquals(initialBalance - amountSpent + amountSold, currentBalance, DELTA);

			assertEquals(0.0, account.getBalance("ADA").getLocked());
			assertEquals(0.0, account.getBalance("USDT").getLocked());

			return currentBalance;

		} else {
			assertEquals(CANCELLED, lossOrder.getStatus());
			assertEquals(FILLED, profitOrder.getStatus());

			assertFalse(lossOrder.isActive());
			assertTrue(profitOrder.isActive());

			assertEquals(0, account.getBalance("ADA").getLocked(), DELTA);

			currentBalance = account.getAmount("USDT");
			assertEquals(initialBalance - amountSpent + quantity1 * (unitPrice + priceIncrement) * 0.999, currentBalance, DELTA);
			return currentBalance;
		}
	}

	@Test
	public void testCancellation() {
		SimulatedAccountManager account = getSimulatedAccountManager();

		account.setAmount("USDT", 100.0);
		long time = 1;

		Order order = submitOrder(account, Order.Side.BUY, LONG, time++, 4.0);
		assertEquals(99.98990001, account.getBalance("USDT").getLocked(), DELTA);

		cancelOrder(account, order, time++);

		order = submitOrder(account, Order.Side.BUY, LONG, time++, 4.0);
		assertNotNull(order);
		assertEquals(99.98990001, account.getBalance("USDT").getLocked(), DELTA);
		executeOrder(account, order, time);

		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);
		assertEquals(0.01009999, account.getBalance("USDT").getFree(), DELTA);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA"), DELTA);
		assertEquals(3.9956004, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);

		order = submitOrder(account, Order.Side.SELL, LONG, time++, 3.9956004);
		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);
		assertEquals(0.01009999, account.getBalance("USDT").getFree(), DELTA);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA"), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(3.9956004, account.getBalance("ADA").getLocked(), DELTA);

		cancelOrder(account, order, time++);

		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);
		assertEquals(0.01009999, account.getBalance("USDT").getFree(), DELTA);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA"), DELTA);
		assertEquals(3.9956004, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);

		order = submitOrder(account, Order.Side.SELL, LONG, time++, 3.9956004);
		executeOrder(account, order, time);
		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);
		assertEquals(99.80021998, account.getBalance("USDT").getFree(), DELTA);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA"), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);
	}

	@Test
	public void testCancellationOfOrdersInBracket() {
		SimulatedAccountManager account = getSimulatedAccountManager(new DefaultOrderManager() {
			@Override
			public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
				if (order.isBuy() && order.isLong() || order.isSell() && order.isShort()) {
					OrderRequest limitSellOnLoss = order.attachToPercentageChange(LIMIT, -1.0);
					OrderRequest takeProfit = order.attachToPercentageChange(LIMIT, 1.0);
				}
			}
		});


		final double MAX = 40.0;
		double initialBalance = 100;
		long time = 0;
		double unitPrice = 0.5;

		account.setAmount("USDT", initialBalance);
		account.configuration.maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.tradingManagers.get("ADAUSDT")[0].trader;

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, ++time, unitPrice, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBracketOrder(usdBalance, trade, 40.0, 0.0, unitPrice, unitPrice, unitPrice);
		usdBalance = account.getAmount("USDT");

		double amountSpent = addFees(quantity1 * unitPrice);

		assertEquals(40.0 / unitPrice * 0.9999 * 0.999, quantity1); //taking offset & fees out
		assertEquals(initialBalance - amountSpent, usdBalance, DELTA); //attached orders submitted, so 1x fees again

		Order parent = trade.position().iterator().next();
		assertEquals(2, parent.getAttachments().size());

		Order profitOrder = null;
		Order lossOrder = null;

		for (Order o : parent.getAttachments()) {
			assertEquals(NEW, o.getStatus());
			assertEquals(parent.getOrderId(), o.getParentOrderId());
			assertFalse(o.isActive());
			if (o.getTriggerPrice() > unitPrice) {
				profitOrder = o;
			} else {
				lossOrder = o;
			}
		}

		assertNotNull(profitOrder);
		assertNotNull(lossOrder);

		assertEquals(parent, profitOrder.getParent());
		assertEquals(parent, lossOrder.getParent());

		assertEquals(0.0, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(79.91200800, account.getBalance("ADA").getLocked(), DELTA);

		assertEquals(60.004039996, account.getBalance("USDT").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("USDT").getShorted(), DELTA);
		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);

		profitOrder.cancel();
		lossOrder.cancel();

		tick(trader, time, 0.5);


		assertEquals(79.91200800, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);

		assertEquals(60.004039996, account.getBalance("USDT").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("USDT").getShorted(), DELTA);
		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);
	}

}
