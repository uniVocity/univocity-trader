package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;

import java.time.*;
import java.util.*;

public final class Client<T> {

	private TradingManager root;
	private CandleRepository candleRepository;
	private Exchange<T, ?> exchange;

	private final List<CandleProcessor<T>> candleProcessors = new ArrayList<>();

	private final AccountManager accountManager;

	public Client(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	public String getId() {
		return accountManager.accountId();
	}

	public String getEmail() {
		return accountManager.email();
	}

	public ZoneId getTimezone() {
		return accountManager.timeZone().toZoneId();
	}

	AccountManager getAccountManager() {
		return accountManager;
	}

	public void initialize(CandleRepository candleRepository, Exchange<T, ?> exchange, SmtpMailSender mailSender) {
		if (accountManager.getAllSymbolPairs().isEmpty()) {
			throw new IllegalStateException("No trade symbols defined for client " + accountManager.accountId());
		}
		this.candleRepository = candleRepository;
		this.exchange = exchange;
		final SymbolPriceDetails priceDetails = new SymbolPriceDetails(exchange, accountManager.getReferenceCurrencySymbol()); //loads price information from exchange

		OrderExecutionToEmail emailNotifier = mailSender != null ? new OrderExecutionToEmail(mailSender) : null;

		Set<Object> allInstances = new HashSet<>();
		initialize(accountManager.configuration, emailNotifier, priceDetails, allInstances);
		accountManager.configuration.tradingGroups().forEach(g -> initialize(g, emailNotifier, priceDetails, allInstances));
		allInstances.clear();
	}

	private void initialize(AbstractTradingGroup<?> group, OrderExecutionToEmail emailNotifier, SymbolPriceDetails priceDetails, Set<Object> allInstances) {
		if (!group.isConfigured()) {
			return;
		}
		if (emailNotifier != null) {
			group.listeners().add(emailNotifier);
		}

		for (Map.Entry<String, String[]> e : group.symbolPairs().entrySet()) {
			String assetSymbol = e.getValue()[0].intern();
			String fundSymbol = e.getValue()[1].intern();

			TradingManager tradingManager = new TradingManager(group, exchange, priceDetails, accountManager, assetSymbol, fundSymbol, Parameters.NULL);
			if (root == null) {
				root = tradingManager;
			}

			Engine engine = new TradingEngine(tradingManager, allInstances);

			CandleProcessor<T> processor = new CandleProcessor<T>(candleRepository, engine, exchange);
			candleProcessors.add(processor);
		}
	}

	void reset() {
		if (candleRepository != null && exchange != null) {
			candleProcessors.clear();
			root = null;
			initialize(candleRepository, exchange, null);
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

	public Map<String, String[]> getAllSymbolPairs() {
		return accountManager.getAllSymbolPairs();
	}
}
