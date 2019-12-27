package com.univocity.trader.account;

import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.*;

public class AccountManagerTest {

	private static final double CLOSE = 0.4379;

	private AccountManager getAccountManager() {
		SimulationAccount cfg = new SimulationConfiguration().account();
		cfg
				.referenceCurrency("USDT")
				.tradeWithPair("ADA", "BNB");

		SimulatedClientAccount clientAccount = new SimulatedClientAccount(cfg);
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

		double funds = account.allocateFunds("ADA");
		assertEquals(funds, 20.0, 0.001);

		cfg.maximumInvestmentPercentagePerAsset(2.0);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 8.0, 0.001);

		cfg.maximumInvestmentAmountPerTrade(6);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 6.0, 0.001);

		cfg.maximumInvestmentPercentagePerTrade(1.0);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 4.0, 0.001);

		cfg.maximumInvestmentAmountPerTrade(3);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 3, 0.001);


		cfg.minimumInvestmentAmountPerTrade(10);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 0.0, 0.001);

	}

	@Test
	public void testFundAllocationPercentageWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentPercentagePerAsset(90.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(100.0, funds, 0.001);

		account.setAmount("USDT", 50);
		account.setAmount("ADA", 50 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(50.0, funds, 0.001);

		account.setAmount("USDT", 10);
		account.setAmount("ADA", 90 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(10.0, funds, 0.001);

		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerAsset(60.0);

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
		account.configuration().maximumInvestmentPercentagePerTrade(40.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(60.0, funds, 0.001); //total funds = 150: 100 USDT + 1 BNB (worth 50 USDT).

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(60.0, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA");
		;
		assertEquals(20.0, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA");
		;
		assertEquals(0.0, funds, 0.001);
	}

	@Test
	public void testFundAllocationAmountPerTradeWithInvestedAmounts() {
		AccountManager account = getAccountManager();

		account.setAmount("USDT", 100);
		account.configuration().maximumInvestmentAmountPerTrade(40.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 60);
		account.setAmount("ADA", 40 / CLOSE);

		funds = account.allocateFunds("ADA");
		assertEquals(40.0, funds, 0.001);

		account.setAmount("USDT", 20);
		account.setAmount("ADA", 80 / CLOSE);

		funds = account.allocateFunds("ADA");
		;
		assertEquals(20.0, funds, 0.001);
		account.setAmount("USDT", 0);
		account.setAmount("ADA", 100 / CLOSE);

		funds = account.allocateFunds("ADA");
		;
		assertEquals(0.0, funds, 0.001);
	}
}