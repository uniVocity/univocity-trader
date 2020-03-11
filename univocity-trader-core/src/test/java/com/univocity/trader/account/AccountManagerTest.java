package com.univocity.trader.account;

import com.univocity.trader.config.*;
import org.junit.*;

import static com.univocity.trader.account.Trade.Side.*;
import static junit.framework.TestCase.*;

public class AccountManagerTest extends OrderFillChecker {

	@Test
	public void testFundAllocationBasics() {
		Balance.balanceUpdateCounts.clear();
		AccountManager account = getAccountManager();
		AccountConfiguration<?> cfg = account.configuration();

		account.setAmount("USDT", 350);
		cfg.maximumInvestmentAmountPerAsset(20.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(20, funds, DELTA);

		cfg.maximumInvestmentPercentagePerAsset(2.0);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(8, funds, DELTA);

		cfg.maximumInvestmentAmountPerTrade(6);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(6, funds, DELTA);

		cfg.maximumInvestmentPercentagePerTrade(1.0);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(4, funds, DELTA);

		cfg.maximumInvestmentAmountPerTrade(3);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(3, funds, DELTA);


		cfg.minimumInvestmentAmountPerTrade(10);
		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, DELTA);

	}

	@Test
	public void testFundAllocationPercentageWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerAsset(90.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(100.0, funds, DELTA);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(50, funds, DELTA);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(10, funds, DELTA);

		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, DELTA);
	}

	@Test
	public void testFundAllocationAmountWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerAsset(60.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(60, funds, DELTA);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(10, funds, DELTA);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, DELTA);
	}

	@Test
	public void testFundAllocationPercentagePerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerTrade(40.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(60, funds, DELTA); //total funds = 150: 100 USDT + 1 BNB (worth 50 USDT).

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(60, funds, DELTA);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		;
		assertEquals(20, funds, DELTA);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, DELTA);
	}

	@Test
	public void testFundAllocationAmountPerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerTrade(40.0);

		double funds = account.allocateFunds("ADA", LONG);
		assertEquals(40.0, funds, DELTA);

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(40.0, funds, DELTA);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(20.00, funds, DELTA);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA", LONG);
		assertEquals(0.0, funds, DELTA);
	}
}