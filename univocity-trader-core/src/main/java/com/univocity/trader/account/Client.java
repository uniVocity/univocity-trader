package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Client<T> extends DefaultConfiguration {

	private static final Set<Object> allInstances = ConcurrentHashMap.newKeySet();

	private final String email;
	private final ZoneId timezone;

	private Exchange api;
	private TradingManager root;
	private ClientAccount clientAccountApi;

	private final List<CandleProcessor<T>> candleProcessors = new ArrayList<>();


	public Client(String email, ZoneId timezone, String referenceCurrencySymbol, ClientAccount clientAccountApi) {
		super(referenceCurrencySymbol);
		this.email = email;
		this.timezone = timezone;
		this.clientAccountApi = clientAccountApi;
	}

	@Override
	protected AccountManager createAccount() {
		return new AccountManager(getReferenceCurrency(), clientAccountApi);
	}

	public AccountConfiguration account() {
		return getAccount();
	}

	public void initialize(Exchange<T> api, SmtpMailSender mailSender) {
		this.api = api;
		if (symbolPairs.isEmpty()) {
			throw new IllegalStateException("No trade symbols defined for client " + email);
		}
		final SymbolPriceDetails priceDetails = new SymbolPriceDetails(api); //loads price information from exchange

		Set<TradingManager> all = new HashSet<>();

		if (mailSender != null) {
			this.listeners().add(new OrderExecutionToEmail(mailSender));
		}

		for (Map.Entry<String, String[]> e : symbolPairs.entrySet()) {
			String assetSymbol = e.getValue()[0];
			String fundSymbol = e.getValue()[1];

			TradingManager tradingManager = new TradingManager(api, priceDetails, getAccount(), listeners, assetSymbol, fundSymbol, Parameters.NULL);
			if (root == null) {
				root = tradingManager;
			}
			all.add(tradingManager);

			Engine engine = new Engine(tradingManager, strategies, monitors);

			CandleProcessor<T> processor = new CandleProcessor<T>(engine, api);
			candleProcessors.add(processor);
		}

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
