package com.univocity.trader.account;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.*;

public class AccountManagerTest {

	private static final double CLOSE = 0.4379;

	private AccountManager getAccountManager() {
		SimulatedClientAccount clientAccount = new SimulatedClientAccount("USDT", SimpleTradingFees.percentage(0.0));
		AccountManager account = clientAccount.getAccount();
		account.setTradedPairs(Collections.singletonList(new String[]{"ADA", "USDT"}));

		TradingManager m = new TradingManager(new SimulatedExchange(account), null, account, null, "ADA", "USDT", Parameters.NULL);
		Trader trader = new Trader(m, null, null, new HashSet<>());
		trader.trade(new Candle(1, 2, 0.04371, 0.4380, 0.4369, CLOSE, 100.0), Signal.NEUTRAL, null);

		return account;
	}

	@Test
	public void testFundAllocationBasics() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 350);
		account.maximumInvestmentAmountPerAsset(20.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(funds, 20.0, 0.001);

		account.maximumInvestmentPercentagePerAsset(2.0);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 7.0, 0.001);

		account.maximumInvestmentAmountPerTrade(6);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 6.0, 0.001);

		account.maximumInvestmentPercentagePerTrade(1.5);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 5.25, 0.001);

		account.maximumInvestmentAmountPerTrade(5);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 5, 0.001);


		account.minimumInvestmentAmountPerTrade(10);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 0.0, 0.001);

	}

	@Test
	public void testFundAllocationPercentageWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.maximumInvestmentPercentagePerAsset(90.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(90.0, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.maximumInvestmentAmountPerAsset(60.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(60.0, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(10.0, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationPercentagePerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.maximumInvestmentPercentagePerTrade(40.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA");;
		assertEquals(20.0, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA");;
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountPerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.maximumInvestmentAmountPerTrade(40.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA");;
		assertEquals(20.0, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA");;
		assertEquals(0.0, funds, 0.001);
	}
}