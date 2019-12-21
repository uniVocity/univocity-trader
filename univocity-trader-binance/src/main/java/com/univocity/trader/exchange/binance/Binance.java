package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.config.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.indicators.base.*;
import com.univocity.trader.simulation.*;

import java.util.function.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Binance extends Configuration<Binance, Account> {

	private Binance() {
		super("binance.properties");
	}

	@Override
	protected ConfigurationGroup[] getAdditionalConfigurationGroups() {
		return new ConfigurationGroup[0];
	}

	@Override
	protected Account newAccountConfiguration() {
		return new Account();
	}

	public static final class Simulator extends AbstractMarketSimulator<Binance, Account> {
		private Simulator(Binance configuration) {
			super(configuration);
		}
	}

	public static final class Trader extends LiveTrader<Candlestick, Binance, Account> {
		private Trader(Binance configuration) {
			super(new BinanceExchange(), configuration);
		}
	}

	public static Simulator simulator() {
		return new Simulator(new Binance().configure());
	}

	public static Trader liveTrader() {
		return new Trader(new Binance().configure());
	}

}
