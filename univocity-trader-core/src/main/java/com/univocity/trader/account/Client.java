package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Client<T> {

	private static final Set<Object> allInstances = ConcurrentHashMap.newKeySet();

	private Exchange exchange;
	private TradingManager root;
	private ClientAccount account;

	private final List<CandleProcessor<T>> candleProcessors = new ArrayList<>();

	private final AccountManager accountManager;

	public Client(ClientAccount account, AccountManager accountManager) {
		this.account = account;
		this.accountManager = accountManager;
	}

	public String getId() {
		return accountManager.configuration().id();
	}

	void registerTradingManager(TradingManager tradingManager) {
		accountManager.register(tradingManager);
	}

	AccountManager getAccountManager(){
		return accountManager;
	}

	Instances<OrderListener> getOrderListeners(){
		return accountManager.configuration().listeners();
	}

	public void initialize(CandleRepository candleRepository, Exchange<T, ?> exchange, SmtpMailSender mailSender) {
		this.exchange = exchange;
		if (accountManager.configuration().symbolPairs().isEmpty()) {
			throw new IllegalStateException("No trade symbols defined for client " + accountManager.configuration().id());
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
