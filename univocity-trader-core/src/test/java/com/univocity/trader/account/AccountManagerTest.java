package com.univocity.trader.account;

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

	@Test
	public void testLongPositionTrading() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerTrade(40.0);

		Trader trader = account.getTraderOf("ADAUSDT");

		tradeOnPrice(trader, 1, 1.0, BUY);
		assertTrue(trader.trades().iterator().hasNext());

		tradeOnPrice(trader, 5, 1.1, NEUTRAL);

		Trade trade = trader.trades().iterator().next();
		assertEquals(9.9, trade.maxChange(), 0.01);
		assertEquals(-0.1, trade.minChange(), 0.01);
		assertEquals(9.9, trade.priceChangePct(), 0.01);
		assertEquals(1.0, trade.averagePrice(), 0.01);
		assertEquals(0.2, trade.breakEvenChange(), 0.01);
		assertEquals(1.1, trade.maxPrice());
		assertEquals(1.0, trade.minPrice());
		assertEquals(2, trade.ticks());
		assertEquals(4, trade.tradeDuration());
		assertEquals(1.1, trade.lastClosingPrice());
		assertEquals(39.91, trade.quantity(), 0.01);
		assertEquals(account.getAmount("ADA"), trade.quantity(), 0.000001);
		assertEquals(60.04, account.getAmount("USDT"), 0.01);

		assertFalse(trade.stopped());
		assertNull(trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(0.0, trade.actualProfitLoss(), 0.00001);
		assertEquals(0.0, trade.actualProfitLossPct(), 0.00001);

		tradeOnPrice(trader, 10, 0.8, BUY);

		assertEquals(trade.averagePrice(), 0.889, 0.001);
		// percentages are calculated using updated average price against max price ever reached since first trade
		assertEquals(23.62, trade.maxChange(), 0.01);
		assertEquals(-10.08, trade.minChange(), 0.01);
		assertEquals(-10.08, trade.priceChangePct(), 0.01);

		assertEquals(0.2, trade.breakEvenChange(), 0.01);
		assertEquals(1.1, trade.maxPrice());
		assertEquals(0.8, trade.minPrice());
		assertEquals(4, trade.ticks());
		assertEquals(10, trade.tradeDuration());
		assertEquals(0.8, trade.lastClosingPrice());
		assertEquals(89.81, trade.quantity(), 0.01);
		assertEquals(trade.quantity(), account.getAmount("ADA"), 0.000001);
		assertEquals(20.08, account.getAmount("USDT"), 0.01);

		assertFalse(trade.stopped());
		assertNull(trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(0.0, trade.actualProfitLoss(), 0.00001);
		assertEquals(0.0, trade.actualProfitLossPct(), 0.00001);

		tradeOnPrice(trader, 20, 0.95, SELL);

		assertEquals(0.889, trade.averagePrice(), 0.001);
		// percentages are calculated using updated average price against max price ever reached since first trade
		assertEquals(23.62, trade.maxChange(), 0.01);
		assertEquals(-10.08, trade.minChange(), 0.01);
		assertEquals(6.76, trade.priceChangePct(), 0.01);

		assertEquals(0.2, trade.breakEvenChange(), 0.01);
		assertEquals(1.1, trade.maxPrice());
		assertEquals(0.8, trade.minPrice());
		assertEquals(5, trade.ticks());
		assertEquals(20, trade.tradeDuration());
		assertEquals(0.95, trade.lastClosingPrice());
		assertEquals(89.81, trade.quantity(), 0.01);
		assertEquals(0.0, account.getAmount("ADA"), 0.000001);
		assertEquals(105.32, account.getAmount("USDT"), 0.01);

		assertFalse(trade.stopped());
		assertEquals("Sell signal", trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(5.408, trade.actualProfitLoss(), 0.001);
		assertEquals(6.768, trade.actualProfitLossPct(), 0.001);

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


	@Test
	public void testShortPositionTrading() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerTrade(40.0);

		Trader trader = account.getTraderOf("ADAUSDT");

		tradeOnPrice(trader, 1, 0.9, SELL);
		assertTrue(trader.trades().iterator().hasNext());

		tradeOnPrice(trader, 5, 1.0, NEUTRAL);

		Trade trade = trader.trades().iterator().next();
		assertEquals(-0.1, trade.maxChange(), 0.01);
		assertEquals(-10.09, trade.minChange(), 0.01);
		assertEquals(-10.09, trade.priceChangePct(), 0.01);
		assertEquals(0.899, trade.averagePrice(), 0.01);
		assertEquals(0.2, trade.breakEvenChange(), 0.01);
		assertEquals(1.0, trade.maxPrice());
		assertEquals(0.9, trade.minPrice());
		assertEquals(2, trade.ticks());
		assertEquals(4, trade.tradeDuration());
		assertEquals(1.0, trade.lastClosingPrice());
		assertEquals(44.40, trade.quantity(), 0.01);
		assertEquals(account.getAmount("ADA"), 0.0, 0.000001);
		assertEquals(trade.quantity(), account.getShortedAmount("ADA"), 0.01);
		assertEquals(40.02, account.getAmount("USDT"), 0.01); //~ 40 bucks trade + 50% margin reserve

		assertFalse(trade.stopped());
		assertNull(trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(0.0, trade.actualProfitLoss(), 0.00001);
		assertEquals(0.0, trade.actualProfitLossPct(), 0.00001);

		tradeOnPrice(trader, 10, 1.2, SELL);

		assertEquals(66.61, trade.quantity(), 0.01);
		assertEquals(account.getAmount("ADA"), 0.0, 0.000001);
		assertEquals(trade.quantity(), account.getShortedAmount("ADA"), 0.01);
		assertEquals(0.013, account.getAmount("USDT"), 0.01); //previous + ~ 22 bucks trade + 50% margin reserve, no more funds.

		assertEquals(0.999, trade.averagePrice(), 0.001);
		// percentages are calculated using updated average price against max price ever reached since first trade
		assertEquals(11.00, trade.maxChange(), 0.01);
		assertEquals(-16.75, trade.minChange(), 0.01);
		assertEquals(-16.75, trade.priceChangePct(), 0.01);

		assertEquals(0.2, trade.breakEvenChange(), 0.01);
		assertEquals(1.2, trade.maxPrice());
		assertEquals(0.9, trade.minPrice());
		assertEquals(4, trade.ticks());
		assertEquals(10, trade.tradeDuration());
		assertEquals(1.2, trade.lastClosingPrice());
		assertEquals(66.61, trade.quantity(), 0.01);
		assertEquals(account.getShortedAmount("ADA"), trade.quantity(), 0.000001);
		assertEquals(0.013, account.getAmount("USDT"), 0.01);

		assertFalse(trade.stopped());
		assertNull(trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(0.0, trade.actualProfitLoss(), 0.00001);
		assertEquals(0.0, trade.actualProfitLossPct(), 0.00001);

		tradeOnPrice(trader, 20, 0.95, SELL);

		assertEquals(105.32, account.getAmount("USDT"), 0.01);
		assertEquals(0.0, account.getAmount("ADA"), 0.000001);


		assertEquals(0.889, trade.averagePrice(), 0.001);
		// percentages are calculated using updated average price against max price ever reached since first trade
		assertEquals(23.62, trade.maxChange(), 0.01);
		assertEquals(-10.08, trade.minChange(), 0.01);
		assertEquals(6.76, trade.priceChangePct(), 0.01);

		assertEquals(0.2, trade.breakEvenChange(), 0.01);
		assertEquals(1.1, trade.maxPrice());
		assertEquals(0.8, trade.minPrice());
		assertEquals(5, trade.ticks());
		assertEquals(20, trade.tradeDuration());
		assertEquals(0.95, trade.lastClosingPrice());
		assertEquals(89.9, trade.quantity(), 0.01);
		assertEquals(0.0, account.getAmount("ADA"), 0.000001);
		assertEquals(105.32, account.getAmount("USDT"), 0.01);

		assertFalse(trade.stopped());
		assertEquals("Rebuy signal", trade.exitReason());
		assertFalse(trade.tryingToExit());
		assertEquals(5.328, trade.actualProfitLoss(), 0.001);
		assertEquals(6.661, trade.actualProfitLossPct(), 0.001);

	}

}