package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.config.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * InteractiveBrokers doesn't require an account login. We simply connect to their Trader Workstation
 * which must be running locally.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class Account extends AccountConfiguration<Account> {

	private Map<String, Contract> tradedContracts = new ConcurrentHashMap<>();
	private Map<String, TradeType> tradeTypes = new ConcurrentHashMap<>();

	public Account(String id) {
		super(id);
	}

	@Override
	protected void readExchangeAccountProperties(String accountId, PropertyBasedConfiguration properties) {

	}

	public Contract tradeWith(SecurityType securityType, String symbol) {
		return tradeWith(securityType, symbol, referenceCurrency());
	}

	public Contract tradeWith(SecurityType securityType, String symbol, String currency) {
		return tradeWith(securityType, symbol, currency, tradeTypes.getOrDefault(symbol + currency, securityType.defaultTradeType()));
	}

	public Contract tradeWith(SecurityType securityType, String symbol, String currency, TradeType tradeType) {
		tradeWithPair(new String[]{symbol, currency});
		String pair = symbol + currency;
		Contract contract = tradedContracts.get(pair);
		if (contract == null) {
			contract = new Contract();
			contract.symbol(symbol);
			contract.secType(securityType.securityCode);
			contract.currency(currency);
			contract.exchange(securityType.defaultExchange);
			tradedContracts.put(pair, contract);
		}
		tradeTypes.put(pair, tradeType);
		return contract;
	}

	public Map<String, Contract> tradedContracts() {
		return Collections.unmodifiableMap(tradedContracts);
	}

	public Map<String, TradeType> tradeTypes() {
		return Collections.unmodifiableMap(tradeTypes);
	}
}