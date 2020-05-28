package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public final class Client<T> {

	private CandleRepository candleRepository;
	private Exchange<T, ?> exchange;

	private final Map<String, CandleProcessor<T>[]> candleProcessors = new ConcurrentHashMap<>();

	private final AccountManager accountManager;
	private OrderExecutionToEmail emailNotifier;

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

		emailNotifier = mailSender != null ? new OrderExecutionToEmail(mailSender) : null;

		Set<Object> allInstances = new HashSet<>();
		accountManager.getAllSymbolPairs().keySet().forEach(s -> accountManager.createTradingManager(s, exchange, emailNotifier, Parameters.NULL));

		Map<String, List<CandleProcessor<T>>> tmp = new HashMap<>();

		accountManager.forEachTradingManager(tradingManager -> {
			Engine engine = new TradingEngine(tradingManager, allInstances);
			CandleProcessor<T> processor = new CandleProcessor<>(candleRepository, engine, exchange, tradingManager.processFullCandlesOnly());
			tmp.computeIfAbsent(engine.getSymbol(), s -> new ArrayList<>()).add(processor);
		});

		allInstances.clear();

		tmp.forEach((k, v) -> candleProcessors.put(k, v.toArray(CandleProcessor[]::new)));
	}

	void reset() {
		if (candleRepository != null && exchange != null) {
			candleProcessors.clear();
			initialize(candleRepository, exchange, null);
		}
	}

	public void sendBalanceEmail(String title) {
		if (emailNotifier != null) {
			emailNotifier.sendBalanceEmail(title, this);
		}
	}

	public void updateBalances() {
		accountManager.updateBalances();
	}

	public void processCandle(String symbol, Candle candle, boolean initializing) {
		CandleProcessor<T>[] processors = candleProcessors.get(symbol);
		for (int i = 0; i < processors.length; i++) {
			processors[i].processCandle(candle, initializing);
		}
	}

	public void processCandle(String symbol, T candle, boolean initializing) {
		CandleProcessor<T>[] processors = candleProcessors.get(symbol);
		for (int i = 0; i < processors.length; i++) {
			processors[i].processCandle(candle, initializing);
		}
	}

	public Map<String, String[]> getAllSymbolPairs() {
		return accountManager.getAllSymbolPairs();
	}
}
