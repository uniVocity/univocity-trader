package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Client<T> {

	private static final Set<Object> allInstances = ConcurrentHashMap.newKeySet();

	private final String email;
	private final ZoneId timezone;

	private Exchange exchange;
	private TradingManager root;
	private ClientAccount account;

	private final List<CandleProcessor<T>> candleProcessors = new ArrayList<>();

	private final AccountManager accountManager;

	public Client(ClientAccount account, AccountConfiguration<?> accountConfiguration) {
		this.email = accountConfiguration.email();
		this.timezone = accountConfiguration.timeZone().toZoneId();
		this.account = account;

		this.accountManager = new AccountManager(account, accountConfiguration);
	}

	public void initialize(Exchange<T, ?> exchange, SmtpMailSender mailSender) {
		this.exchange = exchange;
		if (accountManager.configuration().symbolPairs().isEmpty()) {
			throw new IllegalStateException("No trade symbols defined for client " + email);
		}
		final SymbolPriceDetails priceDetails = new SymbolPriceDetails(exchange); //loads price information from exchange

		Set<TradingManager> all = new HashSet<>();

		if (mailSender != null) {
			accountManager.configuration().listeners().add(new OrderExecutionToEmail(mailSender));
		}

		Set<Object> allInstances = new HashSet<>();
		for (Map.Entry<String, String[]> e : accountManager.configuration().symbolPairs().entrySet()) {
			String assetSymbol = e.getValue()[0];
			String fundSymbol = e.getValue()[1];

			TradingManager tradingManager = new TradingManager(exchange, priceDetails, accountManager, assetSymbol, fundSymbol, Parameters.NULL);
			if (root == null) {
				root = tradingManager;
			}
			all.add(tradingManager);

			Engine engine = new Engine(tradingManager, allInstances);

			CandleProcessor<T> processor = new CandleProcessor<T>(engine, exchange);
			candleProcessors.add(processor);
		}
		allInstances.clear();

		for (TradingManager a : all) {
			a.client = this;
		}
	}

	public void sendBalanceEmail(String title) {
		root.sendBalanceEmail(title, this);
	}

	public void updateBalances() {
		root.updateBalances();
	}

	public void processCandle(String symbol, Candle candle, boolean initializing) {
		candleProcessors.forEach(c -> c.processCandle(symbol, candle, initializing));
	}

	public void processCandle(String symbol, T candle, boolean initializing) {
		candleProcessors.forEach(c -> c.processCandle(symbol, candle, initializing));
	}

	public String getEmail() {
		return email;
	}

	public ZoneId getTimezone() {
		return timezone;
	}

	public Map<String, String[]> getSymbolPairs() {
		return accountManager.configuration().symbolPairs();
	}

//	private static <T> T[] getInstances(String symbol, Map<String, Supplier<T[]>> provider, String description) {
//		Supplier<T[]> supplier = provider.get(symbol);
//		if (supplier == null) {
//			throw new IllegalStateException("Can't trade. No " + description + " provided for symbol " + symbol);
//		}
//
//		T[] instances = supplier.get();
//		if (ArrayUtils.isEmpty(instances)) {
//			throw new IllegalStateException("Can't trade. No " + description + " provided for symbol " + symbol);
//		}
//		for (T instance : instances) {
//			if (allInstances.contains(instance)) {
//				throw new IllegalStateException("Can't trade " + description + " instance provided for symbol " + symbol + " is already in use. Make sure to build a *new* " + description + " object for each symbol and client.");
//			} else {
//				allInstances.add(instance);
//			}
//		}
//		return instances;
//	}

}
