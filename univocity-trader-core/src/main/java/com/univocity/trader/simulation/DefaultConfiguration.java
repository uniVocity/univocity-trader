package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.notification.*;
import com.univocity.trader.strategy.*;
import com.univocity.trader.utils.*;
import org.apache.commons.lang3.*;

import java.util.*;

public abstract class DefaultConfiguration {

	protected final NewInstances<Strategy> strategies = new NewInstances<>(new Strategy[0]);
	protected final NewInstances<StrategyMonitor> monitors = new NewInstances<>(new StrategyMonitor[0]);
	protected final Instances<OrderEventListener> listeners = new Instances<>(new OrderEventListener[0]);
	protected final Map<String, String[]> symbolPairs = new TreeMap<>();
	private final String referenceCurrency;
	private AccountManager account;

	public DefaultConfiguration(String referenceCurrency) {
		if (StringUtils.isBlank(referenceCurrency)) {
			throw new IllegalArgumentException("Reference currency must be provided");
		}
		this.referenceCurrency = referenceCurrency;
	}

	public String getReferenceCurrency() {
		return referenceCurrency;
	}

	public NewInstances<Strategy> strategies() {
		return strategies;
	}

	public NewInstances<StrategyMonitor> monitors() {
		return monitors;
	}

	public Instances<OrderEventListener> listeners() {
		return listeners;
	}

	public void tradeWithPair(String[]... symbolPairs) {
		for (String[] pair : symbolPairs) {
			tradeWithPair(pair[0], pair[1]);
		}
	}

	public void tradeWith(String... assetSymbols) {
		for (String assetSymbol : assetSymbols) {
			tradeWith(assetSymbol);
		}
	}

	public void tradeWith(String assetSymbol) {
		tradeWithPair(assetSymbol, referenceCurrency);
	}

	public void tradeWithPair(String assetSymbol, String fundSymbol) {
		symbolPairs.put(assetSymbol + fundSymbol, new String[]{assetSymbol, fundSymbol});
	}

	protected abstract AccountManager createAccount();

	protected AccountManager getAccount() {
		if (account == null) {
			if (symbolPairs.isEmpty()) {
				throw new IllegalStateException("Please configure traded symbol pairs before the account");
			}
			account = createAccount();
			account.setTradedPairs(populateTradingPairs());
		}
		return account;
	}

	private Collection<String[]> populateTradingPairs() {
		return symbolPairs.values();

		//List<String[]> out = new ArrayList<>();

//		Set<String> tradedSymbols = new HashSet<>();
//		for (String[] symbol : symbols) {
//			out.add(symbol);
//			tradedSymbols.add(symbol[0]);
//		}

//		enable this later - used to allow switching straight into another asset without selling into cash then buying the desired asset with that cash.
//		for (String[] symbol : symbols) {
//			for (String mainTradeSymbol : mainTradeSymbols) {
//				if (tradedSymbols.contains(mainTradeSymbol) && !symbol[0].equals(mainTradeSymbol) && !symbol[1].equals(mainTradeSymbol)) {
//					symbol = symbol.clone();
//					symbol[1] = mainTradeSymbol;
//					out.add(symbol);
//				}
//			}
//		}
//		return out;
	}

	public Map<String, String[]> getSymbolPairs() {
		return symbolPairs;
	}
}

