package com.univocity.trader.exchange.interactivebrokers;

import java.util.*;

import static com.univocity.trader.exchange.interactivebrokers.TickType.*;
import static com.univocity.trader.exchange.interactivebrokers.TradeType.*;

/**
 * Security types with defaults taken from https://interactivebrokers.github.io/tws-api/basic_contracts.html
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum SecurityType {
	FOREX("CASH", "IDEALPRO", new TradeType[]{MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{MidPoint, BidAsk}),
	STOCKS("STK", "SMART", new TradeType[]{ADJUSTED_LAST, TRADES, MIDPOINT, BID, ASK, BID_ASK, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	ETFS("ETF", "SMART", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	INDICES("IND", "DTB", new TradeType[]{TRADES, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY}, new TickType[]{Last, AllLast}),
	CFDS("CFD", "SMART", new TradeType[]{MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{MidPoint, BidAsk}),
	FUTURES("FUT", "GLOBEX", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	OPTIONS("OPT", "BOX", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	FUTURES_OPTIONS("FOP", "GLOBEX", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	BONDS("BOND", "SMART", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK, YIELD_BID, YIELD_ASK, YIELD_BID_ASK, YIELD_LAST}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	MUTUAL_FUNDS("FUND", "FUNDSERV", new TradeType[]{MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{MidPoint, BidAsk}),
	COMMODITIES("CMDTY", "SMART", new TradeType[]{MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{MidPoint, BidAsk}),
	IOPT("IOPT", "SBF", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{Last, AllLast, MidPoint, BidAsk}),
	SPREAD("BAG", "SMART", new TradeType[]{TRADES, MIDPOINT, BID, ASK, BID_ASK}, new TickType[]{Last, AllLast, MidPoint, BidAsk})

	//TODO: found "metals" here: https://interactivebrokers.github.io/tws-api/historical_bars.html, but nowhere else.
	//METALS("???", "???", TRADES,MIDPOINT,BID,ASK,BID_ASK),
	;

	public final String securityCode;
	public final String defaultExchange;
	public final List<TradeType> availableTradeTypes;
	public final List<TickType> availableTickTypes;

	SecurityType(String securityCode, String defaultExchange, TradeType[] availableTradeTypes, TickType[] availableTickTypes) {
		this.securityCode = securityCode;
		this.defaultExchange = defaultExchange;
		this.availableTradeTypes = Arrays.asList(availableTradeTypes);
		this.availableTickTypes = Arrays.asList(availableTickTypes);
	}

	public static SecurityType fromSecurityCode(String securityCode) {
		securityCode = securityCode == null ? null : securityCode.trim().toUpperCase();
		for (SecurityType t : SecurityType.values()) {
			if (t.securityCode.equals(securityCode)) {
				return t;
			}
		}
		throw new IllegalArgumentException("Unknown security code '" + securityCode + "'");
	}

	public List<TradeType> availableTradeTypes() {
		return availableTradeTypes;
	}

	public List<TickType> getAvailableTickTypes() {
		return availableTickTypes;
	}

	public TradeType defaultTradeType() {
		return availableTradeTypes.get(0);
	}

	public TickType defaultTickType() {
		return availableTickTypes.get(0);
	}

	public boolean isTradeTypeAvailable(TradeType tradeType) {
		return availableTradeTypes.contains(tradeType);
	}

	public boolean isTickTypeAvailable(TickType tickType) {
		return availableTickTypes.contains(tickType);
	}
}