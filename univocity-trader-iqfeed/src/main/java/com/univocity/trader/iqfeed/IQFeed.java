package com.univocity.trader.iqfeed;

import com.univocity.trader.*;
import com.univocity.trader.iqfeed.api.domain.candles.*;
import com.univocity.trader.simulation.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class IQFeed implements EntryPoint {

	public static final class Configuration extends com.univocity.trader.config.Configuration<Configuration, Account> {
		private Configuration() {
			super("iqfeed.properties");
		}

		@Override
		protected Account newAccountConfiguration(String id) {
			return new Account(id);
		}
	}

	public static final class Simulator extends MarketSimulator<Configuration, Account> {
		private Simulator() {
			super(new Configuration(), IQFeedExchange::new);
		}
	}

	public static final class Trader extends LiveTrader<IQFeedCandle, Configuration, Account> {
		private Trader() {
			super(new IQFeedExchange(), new Configuration());
		}
	}

	public static Simulator simulator() {
		return new Simulator();
	}

	public static Trader trader() {
		return new Trader();
	}
}
