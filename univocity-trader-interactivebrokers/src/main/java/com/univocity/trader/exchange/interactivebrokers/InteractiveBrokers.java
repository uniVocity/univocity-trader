package com.univocity.trader.exchange.interactivebrokers;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.simulation.*;

import java.util.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class InteractiveBrokers implements EntryPoint {

	public static final class Configuration extends com.univocity.trader.config.Configuration<Configuration, Account> {
		private Configuration() {
			super("ib.properties");
		}

		@Override
		protected Account newAccountConfiguration(String id) {
			return new Account(id);
		}
	}

	public static final class Simulator extends MarketSimulator<Configuration, Account> {
		private Simulator() {
			super(new Configuration(), IB::new);
		}

		@Override
		protected void backfillHistory(Exchange<?, Account> exchange, Collection<String> symbols) {
			for(Account account : configure().accounts()){
				exchange.connectToAccount(account);
			}
			super.backfillHistory(exchange, symbols);
		}
	}

	public static final class Trader extends LiveTrader<Candle, Configuration, Account> {
		private Trader() {
			super(new IB(), new Configuration().updateHistoryBeforeLiveTrading(false));
		}
	}

	public static Simulator simulator() {
		return new Simulator();
	}

	public static Trader trader() {
		return new Trader();
	}
}
