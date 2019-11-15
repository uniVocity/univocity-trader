package com.univocity.trader.account;

import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import org.junit.*;

import java.util.*;

import static junit.framework.TestCase.*;

public class AccountManagerTest {

	private AccountManager account = getAccountManager();

	private AccountManager getAccountManager() {
		SimulatedClientAccount api = new SimulatedClientAccount("USDT", SimpleTradingFees.percentage(0.1));
		AccountManager account = api.getAccount();
		account.setTradedPairs(Collections.singletonList(new String[]{"ADA", "USDT"}));

		TradingManager m = new TradingManager(new SimulatedExchange(account), null, account, null, "ADA", "USDT", Parameters.NULL);
		Trader trader = new Trader(m, null, null);
		trader.trade(new Candle(1, 2, 0.04371, 0.4380, 0.4369, 0.4379, 100.0), Signal.NEUTRAL, null);

		return account;
	}

	@Test
	public void testFundAllocation() {
		account.setAmount("USDT", 350);
		account.maximumInvestmentAmountPerAsset(20.0);

		double funds = account.allocateFunds("ADA");
		assertEquals(funds, 20.0, 0.001);


		account.maximumInvestmentPercentagePerAsset(2.0);
		funds = account.allocateFunds("ADA");
		assertEquals(funds, 7.0, 0.001);
	}

}