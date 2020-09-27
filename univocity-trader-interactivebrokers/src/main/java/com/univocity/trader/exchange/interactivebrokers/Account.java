package com.univocity.trader.exchange.interactivebrokers;

import com.ib.client.*;
import com.univocity.trader.config.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

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

	public Account tradeWith(SecurityType securityType, String symbol, Consumer<Contract> contractConfigurer) {
		return tradeWith(securityType, symbol, referenceCurrency(), contractConfigurer);
	}

	public Account tradeWith(SecurityType securityType, String symbol) {
		return tradeWith(securityType, symbol, (Consumer) null);
	}

	public Account tradeWith(SecurityType securityType, String symbol, String currency) {
		return tradeWith(securityType, symbol, currency, (Consumer) null);
	}

	public Account tradeWith(SecurityType securityType, String symbol, String currency, Consumer<Contract> contractConfigurer) {
		return tradeWith(securityType, symbol, currency, tradeTypes.getOrDefault(symbol + currency, securityType.defaultTradeType()), contractConfigurer);
	}

	public Account tradeWith(SecurityType securityType, String symbol, String currency, TradeType tradeType) {
		return tradeWith(securityType, symbol, currency, tradeType, null);
	}

	public Account tradeWith(SecurityType securityType, String symbol, String currency, TradeType tradeType, Consumer<Contract> contractConfigurer) {
		String pair = symbol + currency;
		if (tradeTypes.containsKey(pair)) {
			tradeTypes.get(pair);
		}

		tradeWithPair(new String[]{symbol, currency});
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
		if (contractConfigurer != null) {
			contractConfigurer.accept(contract);
		}
		return this;
	}

	public Map<String, Contract> tradedContracts() {
		return Collections.unmodifiableMap(tradedContracts);
	}

	public Map<String, TradeType> tradeTypes() {
		return Collections.unmodifiableMap(tradeTypes);
	}
}