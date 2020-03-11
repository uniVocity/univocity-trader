package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.time.*;
import java.util.*;

public class ExchangeClient<T> implements Client {

	private TradingManager root;

	private final List<CandleProcessor<T>> candleProcessors = new ArrayList<>();

	private final AccountManager accountManager;

	public ExchangeClient(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	public String getId() {
		return accountManager.configuration().id();
	}

	void registerTradingManager(TradingManager tradingManager) {
		accountManager.register(tradingManager);
	}

	AccountManager getAccountManager() {
		return accountManager;
	}

	Instances<OrderListener> getOrderListeners() {
		return accountManager.configuration().listeners();
	}

	public void initialize(CandleRepository candleRepository, Exchange<T, ?> exchange, SmtpMailSender mailSender) {
		if (accountManager.configuration().symbolPairs().isEmpty()) {
			throw new IllegalStateException("No trade symbols defined for client " + accountManager.configuration().id());
		}
		final SymbolPriceDetails priceDetails = new SymbolPriceDetails(exchange, accountManager.getReferenceCurrencySymbol()); //loads price information from exchange

		Set<TradingManager> all = new HashSet<>();

		if (mailSender != null) {
			accountManager.configuration().listeners().add(new OrderExecutionToEmail(mailSender));
		}

		Set<Object> allInstances = new HashSet<>();
		for (Map.Entry<String, String[]> e : accountManager.configuration().symbolPairs().entrySet()) {
			String assetSymbol = e.getValue()[0].intern();
			String fundSymbol = e.getValue()[1].intern();

			TradingManager tradingManager = new TradingManager(exchange, priceDetails, accountManager, assetSymbol, fundSymbol, Parameters.NULL);
			if (root == null) {
				root = tradingManager;
			}
			all.add(tradingManager);

			Engine engine = new Engine(tradingManager, allInstances);

			CandleProcessor<T> processor = new CandleProcessor<T>(candleRepository, engine, exchange);
			candleProcessors.add(processor);
		}
		allInstances.clear();
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
		return accountManager.configuration().email();
	}

	public ZoneId getTimezone() {
		return accountManager.configuration().timeZone().toZoneId();
	}

	public Map<String, String[]> getSymbolPairs() {
		return accountManager.configuration().symbolPairs();
	}
}
