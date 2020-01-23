package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import org.junit.*;

import java.util.*;

import static com.univocity.trader.indicators.Signal.*;
import static junit.framework.TestCase.*;

public class AccountManagerTest {

	private static final double CLOSE = 0.4379;

	private AccountManager getAccountManager() {
		SimulationConfiguration configuration = new SimulationConfiguration();
		SimulationAccount accountCfg = new SimulationConfiguration().account();
		accountCfg
				.referenceCurrency("USDT")
				.tradeWithPair("ADA", "BNB")
				.enableShorting(150);

		SimulatedClientAccount clientAccount = new SimulatedClientAccount(accountCfg, configuration.simulation());
		AccountManager account = clientAccount.getAccount();

		TradingManager m = new TradingManager(new SimulatedExchange(account), null, account, "ADA", "USDT", Parameters.NULL);
		Trader trader = new Trader(m, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 0.04371, 0.4380, 0.4369, CLOSE, 100.0), Signal.NEUTRAL, null);

		m = new TradingManager(new SimulatedExchange(account), null, account, "BNB", "USDT", Parameters.NULL);
		trader = new Trader(m, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 50, 50, 50, 50, 100.0), Signal.NEUTRAL, null);

		account.setAmount("BNB", 1);

		return account;
	}

	@Test
	public void testFundAllocationBasics() {
		AccountManager account = getAccountManager();
		AccountConfiguration<?> cfg = account.configuration();

		account.setAmount("USDT", 350);
		cfg.maximumInvestmentAmountPerAsset(20.0);

		double funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(funds, 19.98, 0.001);

		cfg.maximumInvestmentPercentagePerAsset(2.0);
		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(funds, 7.992, 0.001);

		cfg.maximumInvestmentAmountPerTrade(6);
		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(funds, 5.994, 0.001);

		cfg.maximumInvestmentPercentagePerTrade(1.0);
		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(funds, 3.996, 0.001);

		cfg.maximumInvestmentAmountPerTrade(3);
		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(funds, 2.997, 0.001);


		cfg.minimumInvestmentAmountPerTrade(10);
		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(funds, 0.0, 0.001);

	}

	@Test
	public void testFundAllocationPercentageWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerAsset(90.0);

		double funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(99.9, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(49.95, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(9.99, funds, 0.001);

		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerAsset(60.0);

		double funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(59.94, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(9.99, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationPercentagePerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerTrade(40.0);

		double funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(59.94, funds, 0.001); //total funds = 150: 100 USDT + 1 BNB (worth 50 USDT).

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(59.94, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		;
		assertEquals(19.98, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountPerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerTrade(40.0);

		double funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(39.96, funds, 0.001);

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		assertEquals(39.96, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		;
		assertEquals(19.98, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", Trade.Side.LONG);
		;
		assertEquals(0.0, funds, 0.001);
	}

	private double getInvestmentAmount(Trader trader, double totalSpent) {
		final TradingFees fees = trader.tradingFees();
		return fees.takeFee(totalSpent, Order.Type.LIMIT, Order.Side.BUY);
	}

	private double checkTradeAfterLongBuy(double usdBalanceBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();

		double amountAfterFees = getInvestmentAmount(trader, totalSpent);
		double amountToInvest = getInvestmentAmount(trader, amountAfterFees); //take fees again to ensure there are funds for fees when closing
		double quantityAfterFees = (amountToInvest / unitPrice) * 0.9999; //quantity adjustment to ensure exchange doesn't reject order for mismatching decimals

		double totalQuantity = quantityAfterFees + previousQuantity;

		checkLongTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), 0.01);

		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(totalQuantity, account.getAmount("ADA"), 0.001);
		assertEquals(usdBalanceBeforeTrade - amountAfterFees, account.getAmount("USDT"), 0.01);

		return quantityAfterFees;
	}

	private void checkTradeAfterLongSell(double usdBalanceBeforeTrade, Trade trade, double quantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();
		final TradingFees fees = trader.tradingFees();

		double totalToReceive = quantity * unitPrice;

		final double receivedAfterFees = fees.takeFee(totalToReceive, Order.Type.LIMIT, Order.Side.SELL);

		checkLongTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(quantity, trade.quantity(), 0.01);
		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), 0.001);
		assertEquals(usdBalanceBeforeTrade + receivedAfterFees, account.getAmount("USDT"), 0.01);
	}


	private void checkLongTradeStats(Trade trade, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		final double change = ((unitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;
		final double minChange = ((minUnitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;
		final double maxChange = ((maxUnitPrice - trade.averagePrice()) / trade.averagePrice()) * 100.0;

		assertEquals(maxChange, trade.maxChange(), 0.01);
		assertEquals(minChange, trade.minChange(), 0.01);
		assertEquals(change, trade.priceChangePct(), 0.01);
		assertEquals(maxUnitPrice, trade.maxPrice());
		assertEquals(minUnitPrice, trade.minPrice());
		assertEquals(unitPrice, trade.lastClosingPrice());

	}

	@Test
	public void testLongPositionTrading() {
		AccountManager account = getAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration().maximumInvestmentAmountPerTrade(MAX);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 1, 1.0, BUY);
		final Trade trade = trader.trades().iterator().next();

		double quantity1 = checkTradeAfterLongBuy(usdBalance, trade, MAX, 0.0, 1.0, 1.0, 1.0);
		tradeOnPrice(trader, 5, 1.1, NEUTRAL);
		checkLongTradeStats(trade, 1.1, 1.1, 1.0);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 10, 0.8, BUY);
		double quantity2 = checkTradeAfterLongBuy(usdBalance, trade, MAX, quantity1, 0.8, 1.1, 0.8);

		double averagePrice = ((quantity1 * 1.0) + (quantity2 * 0.8)) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), 0.001);

		usdBalance = account.getAmount("USDT");
		tradeOnPrice(trader, 20, 0.95, SELL);
		checkTradeAfterLongSell(usdBalance, trade, (quantity1 + quantity2), 0.95, 1.1, 0.8);
		assertEquals(averagePrice, trade.averagePrice(), 0.001); //average price is about 0.889

		assertFalse(trade.stopped());
		assertEquals("Sell signal", trade.exitReason());
		assertFalse(trade.tryingToExit());

		checkProfitLoss(trade, initialBalance, (quantity1 * 1.0) + (quantity2 * 0.8));
	}

	private void checkProfitLoss(Trade trade, double initialBalance, double totalInvested) {
		Trader trader = trade.trader();
		AccountManager account = trader.tradingManager.getAccount();

		double finalBalance = account.getAmount("USDT");
		double profitLoss = finalBalance - initialBalance;
		assertEquals(profitLoss, trade.actualProfitLoss(), 0.001);

		double invested = totalInvested + trader.tradingFees().feesOnAmount(totalInvested, Order.Type.LIMIT, Order.Side.SELL);
		double profitLossPercentage = ((profitLoss / invested)) * 100.0;
		assertEquals(profitLossPercentage, trade.actualProfitLossPct(), 0.001);
	}

	private void tradeOnPrice(Trader trader, long time, double price, Signal signal) {
		Candle next = newTick(time, price);
		trader.trade(next, signal, null);
		if (signal != Signal.NEUTRAL) {
			trader.tradingManager.updateOpenOrders(trader.symbol(), next = newTick(time + 1, price));
			trader.trade(next, Signal.NEUTRAL, null);
		}
	}

	private Candle newTick(long time, double price) {
		return new Candle(time, time, price, price, price, price, 100.0);
	}

	private double checkTradeAfterShortSell(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double totalSpent, double previousQuantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();

		double amountToInvest = getInvestmentAmount(trader, totalSpent);
		double feesPaid = totalSpent - amountToInvest;
		double quantityAfterFees = (amountToInvest / unitPrice);

		double totalQuantity = quantityAfterFees + previousQuantity;

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(totalQuantity, trade.quantity(), 0.01);

		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), 0.001);
		assertEquals(totalQuantity, account.getShortedAmount("ADA"), 0.001);

		double inReserve = account.marginReserveFactorPct() * amountToInvest;
		assertEquals(inReserve + usdReservedBeforeTrade, account.getMarginReserve("USDT", "ADA").doubleValue(), 0.001);

		double movedToReserve = inReserve - amountToInvest;
		double freeBalance = usdBalanceBeforeTrade - (movedToReserve + feesPaid);
		assertEquals(freeBalance, account.getAmount("USDT"), 0.01);

		return quantityAfterFees;
	}

	private void checkShortTradeStats(Trade trade, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		final double change = ((trade.averagePrice() - unitPrice) / trade.averagePrice()) * 100.0;
		final double minChange = ((trade.averagePrice() - maxUnitPrice) / trade.averagePrice()) * 100.0;
		final double maxChange = ((trade.averagePrice() - minUnitPrice) / trade.averagePrice()) * 100.0;

		assertEquals(maxChange, trade.maxChange(), 0.001);
		assertEquals(minChange, trade.minChange(), 0.001);
		assertEquals(change, trade.priceChangePct(), 0.001);
		assertEquals(maxUnitPrice, trade.maxPrice());
		assertEquals(minUnitPrice, trade.minPrice());
		assertEquals(unitPrice, trade.lastClosingPrice());
	}

	private void checkTradeAfterShortBuy(double usdBalanceBeforeTrade, double usdReservedBeforeTrade, Trade trade, double quantity, double unitPrice, double maxUnitPrice, double minUnitPrice) {
		Trader trader = trade.trader();
		final TradingFees fees = trader.tradingFees();

		checkShortTradeStats(trade, unitPrice, maxUnitPrice, minUnitPrice);

		assertEquals(quantity, trade.quantity(), 0.01);
		AccountManager account = trader.tradingManager.getAccount();
		assertEquals(0.0, account.getAmount("ADA"), 0.001);

		assertEquals(0.0, account.getBalance("ADA").getFreeAmount(), 0.01);
		assertEquals(0.0, account.getBalance("ADA").getLocked().doubleValue(), 0.01);
		assertEquals(0.0, account.getBalance("ADA").getShortedAmount(), 0.01);
		assertEquals(0.0, account.getMarginReserve("USDT", "ADA").doubleValue(), 0.01);

		double pricePaid = quantity * unitPrice;
		double rebuyCostAfterFees = pricePaid + fees.feesOnAmount(pricePaid, Order.Type.LIMIT, Order.Side.BUY);

		double tradeProfit = usdReservedBeforeTrade - rebuyCostAfterFees;
		double netAccountBalance = usdBalanceBeforeTrade + tradeProfit;

		assertEquals(netAccountBalance, account.getAmount("USDT"), 0.01);
	}

	@Test
	public void testShortPositionTrading() {
		AccountManager account = getAccountManager();

		final double MAX = 40.0;
		final double initialBalance = 100;

		account.setAmount("USDT", initialBalance);
		account.configuration()
				.maximumInvestmentAmountPerTrade(MAX)
				.minimumInvestmentAmountPerTrade(10.0);

		Trader trader = account.getTraderOf("ADAUSDT");

		double usdBalance = account.getAmount("USDT");
		double reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 1, 0.9, SELL);
		Trade trade = trader.trades().iterator().next();
		double quantity1 = checkTradeAfterShortSell(usdBalance, reservedBalance, trade, MAX, 0.0, 0.9, 0.9, 0.9);

		tradeOnPrice(trader, 5, 1.0, NEUTRAL);
		checkShortTradeStats(trade, 1.0, 1.0, 0.9);

		usdBalance = account.getAmount("USDT");
		reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 10, 1.2, SELL);
		double quantity2 = checkTradeAfterShortSell(usdBalance, reservedBalance, trade, MAX, quantity1, 1.2, 1.2, 0.9);

		//average price calculated to include fees to exit
		double averagePrice = getInvestmentAmount(trader, ((quantity1 * 0.9) + (quantity2 * 1.2))) / (quantity1 + quantity2);
		assertEquals(averagePrice, trade.averagePrice(), 0.001);

		usdBalance = account.getAmount("USDT");
		reservedBalance = account.getMarginReserve("USDT", "ADA").doubleValue();
		tradeOnPrice(trader, 20, 0.1, BUY);

		checkTradeAfterShortBuy(usdBalance, reservedBalance, trade, quantity1 + quantity2, 0.1, 1.2, 0.1);

		assertFalse(trade.stopped());
		assertEquals("Buy signal", trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(72.062, trade.actualProfitLoss(), 0.001);
		assertEquals(90.258, trade.actualProfitLossPct(), 0.001);
	}

}