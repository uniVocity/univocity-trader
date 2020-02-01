package com.univocity.trader.exchange.interactivebrokers;

import java.util.*;

import static com.univocity.trader.exchange.interactivebrokers.TradeType.*;

/**
 * Security types with defaults taken from
 * https://interactivebrokers.github.io/tws-api/basic_contracts.html
 *
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum SecurityType {
	FOREX("CASH", "IDEALPRO", MIDPOINT, BID, ASK, BID_ASK),
	STOCKS("STK", "SMART", ADJUSTED_LAST, TRADES, MIDPOINT, BID, ASK, BID_ASK, HISTORICAL_VOLATILITY,
			OPTION_IMPLIED_VOLATILITY),
	ETFS("ETF", "SMART", TRADES, MIDPOINT, BID, ASK, BID_ASK, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY),
	INDICES("IND", "DTB", TRADES, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY),
	CFDS("CFD", "SMART", MIDPOINT, BID, ASK, BID_ASK), FUTURES("FUT", "GLOBEX", TRADES, MIDPOINT, BID, ASK, BID_ASK),
	OPTIONS("OPT", "BOX", TRADES, MIDPOINT, BID, ASK, BID_ASK),
	FUTURES_OPTIONS("FOP", "GLOBEX", TRADES, MIDPOINT, BID, ASK, BID_ASK),
	BONDS("BOND", "SMART", TRADES, MIDPOINT, BID, ASK, BID_ASK, YIELD_BID, YIELD_ASK, YIELD_BID_ASK, YIELD_LAST),
	MUTUAL_FUNDS("FUND", "FUNDSERV", MIDPOINT, BID, ASK, BID_ASK),
	COMMODITIES("CMDTY", "SMART", MIDPOINT, BID, ASK, BID_ASK),
	IOPT("IOPT", "SBF", TRADES, MIDPOINT, BID, ASK, BID_ASK),
	SPREAD("BAG", "SMART", TRADES, MIDPOINT, BID, ASK, BID_ASK)

	// TODO: found "metals" here:
	// https://interactivebrokers.github.io/tws-api/historical_bars.html, but
	// nowhere else.
	// METALS("???", "???", TRADES,MIDPOINT,BID,ASK,BID_ASK),
	;

	public final String securityCode;
	public final String defaultExchange;
	public final List<TradeType> availableTradeTypes;

	SecurityType(String securityCode, String defaultExchange, TradeType... availableTradeTypes) {
		this.securityCode = securityCode;
		this.defaultExchange = defaultExchange;
		this.availableTradeTypes = Arrays.asList(availableTradeTypes);
	}

	public List<TradeType> availableTradeTypes() {
		return availableTradeTypes;
	}

	public TradeType defaultTradeType() {
		return availableTradeTypes.get(0);
	}

	public boolean isTradeTypeAvailable(TradeType tradeType) {
		return availableTradeTypes.contains(tradeType);
	}
}