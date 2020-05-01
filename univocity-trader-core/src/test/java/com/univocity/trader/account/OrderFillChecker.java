package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;

import java.util.*;
import java.util.function.*;

import static junit.framework.TestCase.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class OrderFillChecker {
	static final double CLOSE = 0.4379;
	static final double DELTA = 0.000000000001;

	SimulatedAccountManager getSimulatedAccountManager() {
		return getSimulatedAccountManager(null);
	}

	protected void configure(SimulationConfiguration configuration) {

	}

	SimulatedAccountManager getSimulatedAccountManager(OrderManager orderManager) {
		SimulationConfiguration configuration = new SimulationConfiguration();

		SimulationAccount accountCfg = new SimulationConfiguration().account();
		accountCfg
				.referenceCurrency("USDT")
				.tradeWithPair("ADA", "BNB")
				.tradeWith("ADA", "BNB")
				.enableShorting();

		if (orderManager != null) {
			accountCfg.orderManager(orderManager);
		}

		configure(configuration);

		SimulatedClientAccount clientAccount = new SimulatedClientAccount(accountCfg, configuration.simulation());
		SimulatedAccountManager account = clientAccount.getAccount();

		TradingManager m = new TradingManager(new SimulatedExchange(account), null, account, "ADA", "USDT", Parameters.NULL);
		Trader trader = new Trader(m, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 0.04371, 0.4380, 0.4369, CLOSE, 100.0), Signal.NEUTRAL, null);

		m = new TradingManager(new SimulatedExchange(account), null, account, "BNB", "USDT", Parameters.NULL);
		trader = new Trader(m, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 50, 50, 50, 50, 100.0), Signal.NEUTRAL, null);

		account.setAmount("BNB", 1);

		return account;
	}

	void tradeOnPrice(Trader trader, long time, double price, Signal signal, boolean cancel) {
		Candle next = newTick(time, price);
		trader.trade(next, signal, null);
		if (signal != Signal.NEUTRAL) {
			if (cancel) {
				trader.trades().iterator().next().position().forEach(Order::cancel);
			}
			next = tick(trader, time, price);
			trader.trade(next, Signal.NEUTRAL, null);
		}
	}

	Candle tick(Trader trader, long time, double price) {
		Candle out = newTick(time + 1, price);
		trader.tradingManager.updateOpenOrders(trader.symbol(), out);
		return out;
	}

	void checkProfitLoss(Trade trade, double initialBalance, double totalInvested) {
		Trader trader = trade.trader();
		SimulatedAccountManager account = (SimulatedAccountManager) trader.tradingManager.getAccount();

		double finalBalance = account.getAmount("USDT");
		double profitLoss = finalBalance - initialBalance;
		assertEquals(profitLoss, trade.actualProfitLoss(), DELTA);

		double invested = totalInvested + trader.tradingFees().feesOnAmount(totalInvested, Order.Type.LIMIT, Order.Side.SELL);
		double profitLossPercentage = ((profitLoss / invested)) * 100.0;
		assertEquals(profitLossPercentage, trade.actualProfitLossPct(), DELTA);
	}

	double calculateBuyingQuantity(double usdBalanceBeforeTrade, double spendingLimit, double unitPrice) {
		if (spendingLimit > usdBalanceBeforeTrade) {
			spendingLimit = usdBalanceBeforeTrade;
		}
		double fees = feesOn(spendingLimit);
		spendingLimit = spendingLimit - fees;

		double quantity = (spendingLimit / unitPrice) * 0.9999; //quantity adjustment to ensure exchange doesn't reject order for mismatching decimals
		return quantity;
	}

	double addFees(double total) {
		return total + feesOn(total);
	}

	double subtractFees(double total) {
		return total - feesOn(total);
	}

	double feesOn(double total) {
		return total * 0.001;
	}


	double checkTradeAfterLongBuy(double usdBalanceBeforeTrade, Trade trade, double spendingLimit, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice, Function<SimulatedAccountManager, Double> assetBalance) {
		Trader trader = trade.trader();

		double quantity = calculateBuyingQuantity(usdBalanceBeforeTrade, spendingLimit, unitPrice);
		double totalCost = quantity * unitPrice;
		double fees = feesOn(totalCost);

		double totalQuantity = quantity + previousQuantity;

		checkLongTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), DELTA);

		SimulatedAccountManager account = (SimulatedAccountManager) trader.tradingManager.getAccount();
		assertEquals(totalQuantity, assetBalance.apply(account), DELTA);

		double balance = account.getAmount("USDT");
		assertEquals(usdBalanceBeforeTrade - (totalCost + fees), balance, DELTA);

		return quantity;
	}

	void checkTradeAfterLongSell(double usdBalanceBeforeTrade, Trade trade, double quantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();
		final TradingFees fees = trader.tradingFees();

		double totalToReceive = quantity * unitPrice;

		final double receivedAfterFees = fees.takeFee(totalToReceive, Order.Type.LIMIT, Order.Side.SELL);

		checkLongTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(quantity, trade.quantity(), DELTA);
		SimulatedAccountManager account = (SimulatedAccountManager)trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), DELTA);
		assertEquals(usdBalanceBeforeTrade + receivedAfterFees, account.getAmount("USDT"), DELTA);
	}

	double checkTradeAfterLongBuy(double usdBalanceBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		return checkTradeAfterLongBuy(usdBalanceBeforeTrade, trade, totalSpent, previousQuantity, unitPrice, maxUnitPrice, minUnitPrice,
				(account) -> account.getAmount("ADA"));
	}

	double checkTradeAfterLongBracketOrder(double usdBalanceBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		return checkTradeAfterLongBuy(usdBalanceBeforeTrade, trade, totalSpent, previousQuantity, unitPrice, maxUnitPrice, minUnitPrice,
				//bracket order locks amount bought to sell it back in two opposing orders.
				//locked balance must be relative to amount bought in parent order, and both orders share the same locked balance.
				(account) -> account.getBalance("ADA").getLocked());

	}


	void checkLongTradeStats(Trade trade, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		final double change = ((unitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;
		final double minChange = ((minUnitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;
		final double maxChange = ((maxUnitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;

		assertEquals(maxChange, trade.maxChange(), DELTA);
		assertEquals(minChange, trade.minChange(), DELTA);
		assertEquals(change, trade.priceChangePct(), DELTA);
		assertEquals(maxUnitPrice, trade.maxPrice(), DELTA);
		assertEquals(minUnitPrice, trade.minPrice(), DELTA);
		assertEquals(unitPrice, trade.lastClosingPrice(), DELTA);

	}

	void checkShortTradeStats(Trade trade, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		final double change = ((trade.averagePrice() - unitPrice) / trade.averagePrice()) * 100.0;
		final double minChange = ((trade.averagePrice() - maxUnitPrice) / trade.averagePrice()) * 100.0;
		final double maxChange = ((trade.averagePrice() - minUnitPrice) / trade.averagePrice()) * 100.0;

		assertEquals(maxChange, trade.maxChange(), DELTA);
		assertEquals(minChange, trade.minChange(), DELTA);
		assertEquals(change, trade.priceChangePct(), DELTA);
		assertEquals(maxUnitPrice, trade.maxPrice(), DELTA);
		assertEquals(minUnitPrice, trade.minPrice(), DELTA);
		assertEquals(unitPrice, trade.lastClosingPrice(), DELTA);
	}

	void checkTradeAfterShortBuy(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double quantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();
		final TradingFees fees = trader.tradingFees();

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(quantity, trade.quantity(), DELTA);
		SimulatedAccountManager account = (SimulatedAccountManager)trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), DELTA);

		assertEquals(0.0, account.getBalance("ADA").getFree(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);
		assertEquals(0.0, account.getBalance("ADA").getShorted(), DELTA);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA"), DELTA);

		double pricePaid = quantity * unitPrice;
		double rebuyCostAfterFees = pricePaid + fees.feesOnAmount(pricePaid, Order.Type.LIMIT, Order.Side.BUY);

		double accountBalanceTakenForMargin = (usdReservedBeforeTrade - usdReservedBeforeTrade / 1.5);
		double tradeProfit = usdReservedBeforeTrade - rebuyCostAfterFees;
		double netAccountBalance = usdBalanceBeforeTrade + tradeProfit;

		assertEquals(netAccountBalance, account.getAmount("USDT"), DELTA);
	}


	double checkTradeAfterBracketShortSell(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();

		double quantityAfterFees = (totalSpent / unitPrice);

		double totalQuantity = quantityAfterFees + previousQuantity;

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), DELTA);

		SimulatedAccountManager account = (SimulatedAccountManager)trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), DELTA);
		assertEquals(totalQuantity, account.getShortedAmount("ADA"), DELTA); //orders submitted to buy it all back
		assertEquals(0.0, account.getBalance("ADA").getLocked(), DELTA);

		double reserve = account.marginReserveFactorPct() * totalSpent;
		double inReserve = reserve - feesOn(totalSpent);
		assertEquals(inReserve + usdReservedBeforeTrade, account.getMarginReserve("USDT", "ADA"), DELTA);

		double movedToReserve = reserve - totalSpent;
		double freeBalance = usdBalanceBeforeTrade - movedToReserve;
		assertEquals(freeBalance, account.getAmount("USDT"), DELTA);

		return quantityAfterFees;
	}


	double checkTradeAfterShortSell(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();

		double feesPaid = feesOn(totalSpent);
		double quantityAfterFees = (totalSpent / unitPrice);

		double totalQuantity = quantityAfterFees + previousQuantity;
		if (totalSpent + feesPaid > usdBalanceBeforeTrade) {
			totalSpent = (usdBalanceBeforeTrade - feesPaid) * 0.9999;
			feesPaid = feesOn(totalSpent);
			quantityAfterFees = totalSpent / unitPrice;
			totalQuantity = quantityAfterFees + previousQuantity;
		}

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), DELTA);

		SimulatedAccountManager account = (SimulatedAccountManager) trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), DELTA);
		assertEquals(totalQuantity, account.getShortedAmount("ADA"), DELTA);

		double inReserve = account.marginReserveFactorPct() * totalSpent;
		assertEquals(inReserve + usdReservedBeforeTrade - feesPaid, account.getMarginReserve("USDT", "ADA"), DELTA);

		double movedToReserve = inReserve - totalSpent;
		double freeBalance = usdBalanceBeforeTrade - (movedToReserve);
		//+ feesPaid
		assertEquals(freeBalance, account.getAmount("USDT"), DELTA);
		assertEquals(0.0, account.getBalance("USDT").getLocked(), DELTA);

		return quantityAfterFees;
	}


	void tradeOnPrice(Trader trader, long time, double price, Signal signal) {
		tradeOnPrice(trader, time, price, signal, false);
	}

	Candle newTick(long time, double price) {
		return new Candle(time, time, price, price, price, price, 33.0);
	}

	void executeOrder(SimulatedAccountManager account, Order order, long time) {
		executeOrder(account, order, 25.0, time);
	}

	void executeOrder(SimulatedAccountManager account, Order order, double price, long time) {
		Trader trader = account.getTraderOf(order.getSymbol());
		trader.tradingManager.updateOpenOrders(trader.symbol(), newTick(time, price));
	}

	void cancelOrder(SimulatedAccountManager account, Order order, long time) {
		cancelOrder(account, order, 25, time);
	}

	void cancelOrder(SimulatedAccountManager account, Order order, double price, long time) {
		Trader trader = account.getTraderOf("ADAUSDT");
		order.cancel();
		trader.tradingManager.updateOpenOrders(trader.symbol(), newTick(time, price));
	}

	void assertNoChangeInFunds(SimulatedAccountManager account, String symbol, String marginSymbol, double initialBalance) {
		assertEquals(initialBalance, account.getBalance(symbol).getFree(), DELTA);
		assertEquals(0.0, account.getBalance(symbol).getLocked(), DELTA);
		assertEquals(0.0, account.getBalance(symbol).getShorted(), DELTA);
		assertEquals(0.0, account.getBalance(symbol).getMarginReserve(marginSymbol), DELTA);
	}


	Order submitOrder(SimulatedAccountManager account, Order.Side orderSide, Trade.Side tradeSide, long time, double units) {
		return submitOrder(account, orderSide, tradeSide, time, units, 25, null);
	}

	Order submitOrder(SimulatedAccountManager account, Order.Side orderSide, Trade.Side tradeSide, long time, double units, double price, OrderManager orderManager) {
		return submitOrder(account, "ADA", orderSide, tradeSide, time, units, price, orderManager);
	}

	Order submitOrder(SimulatedAccountManager account, String symbol, Order.Side orderSide, Trade.Side tradeSide, long time, double units, double price, OrderManager orderManager) {
		Candle next = newTick(time, 25.0);
		OrderRequest req = new OrderRequest(symbol, "USDT", orderSide, tradeSide, time, null);
		req.setQuantity(units);
		req.setPrice(price);

		if (orderManager != null) {
			Trader trader = account.getTraderOf(symbol + "USDT");
			trader.latestCandle = next;
			orderManager.prepareOrder(null, null, req, trader);
		}

		Order order = account.executeOrder(req);

		return order;
	}
}
