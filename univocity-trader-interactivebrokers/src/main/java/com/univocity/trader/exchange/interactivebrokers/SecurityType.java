package com.univocity.trader.exchange.interactivebrokers;

/**
 * Security types with defaults taken from https://interactivebrokers.github.io/tws-api/basic_contracts.html
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum SecurityType {
	FOREX("CASH", "IDEALPRO"),
	STOCKS("STK", "SMART"),
	INDEXES("IND", "DTB"),
	CFDS("CFD", "SMART"),
	FUTURES("FUT", "GLOBEX"),
	OPTIONS("OPT", "BOX"),
	FUTURES_OPTIONS("FOP", "GLOBEX"),
	BONDS("BOND", "SMART"),
	MUTUAL_FUNDS("FUND", "FUNDSERV"),
	COMMODITIES("CMDTY", "SMART"),
	IOPT("IOPT", "SBF");

	public final String securityCode;
	public final String defaultExchange;

	SecurityType(String securityCode, String defaultExchange) {
		this.securityCode = securityCode;
		this.defaultExchange = defaultExchange;
	}
}
