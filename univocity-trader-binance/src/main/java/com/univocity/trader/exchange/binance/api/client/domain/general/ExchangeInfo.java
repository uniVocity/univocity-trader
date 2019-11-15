package com.univocity.trader.exchange.binance.api.client.domain.general;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.exception.*;
import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.*;

import java.util.*;

/**
 * Current exchange trading rules and symbol information.
 * https://github.com/binance-exchange/binance-official-api-docs/blob/master/rest-api.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeInfo {

	private String timezone;

	private Long serverTime;

	private List<RateLimit> rateLimits;

	// private List<String> exchangeFilters;

	private List<SymbolInfo> symbols;

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Long getServerTime() {
		return serverTime;
	}

	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}

	public List<RateLimit> getRateLimits() {
		return rateLimits;
	}

	public void setRateLimits(List<RateLimit> rateLimits) {
		this.rateLimits = rateLimits;
	}

	public List<SymbolInfo> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<SymbolInfo> symbols) {
		this.symbols = symbols;
	}

	/**
	 * @param symbol the symbol to obtain information for (e.g. ETHBTC)
	 * @return symbol exchange information
	 */
	public SymbolInfo getSymbolInfo(String symbol) {
		return symbols.stream().filter(symbolInfo -> symbolInfo.getSymbol().equals(symbol))
				.findFirst()
				.orElseThrow(() -> new BinanceApiException("Unable to obtain information for symbol " + symbol));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
				.append("timezone", timezone)
				.append("serverTime", serverTime)
				.append("rateLimits", rateLimits)
				.append("symbols", symbols)
				.toString();
	}
}
