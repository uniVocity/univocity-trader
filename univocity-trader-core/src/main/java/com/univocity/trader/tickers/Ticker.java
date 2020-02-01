package com.univocity.trader.tickers;

import com.univocity.parsers.annotations.*;

/**
 * @author tom@khubla.com
 */
public class Ticker {

	public enum Type {
		crypto, fiat, reference, stock, option
	}

	@Parsed
	private String symbol;

	@Parsed
	private Type type;

	@Parsed
	private String description;

	// TODO: add symbol information details as well (see class
	// com.univocity.trader.candles.SymbolInformation)

	private Ticker() {
	}

	public String getDescription() {
		return description;
	}

	public String getSymbol() {
		return symbol;
	}

	public Type getType() {
		return type;
	}
}