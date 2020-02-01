package com.univocity.trader.exchange.interactivebrokers;

/**
 * Available trade types (to be used when receiving candles/ticks) per security
 * as described in:
 * https://interactivebrokers.github.io/tws-api/historical_bars.html#available_products_hd
 *
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public enum TradeType {
	TRADES, MIDPOINT, BID, ASK, BID_ASK, ADJUSTED_LAST, HISTORICAL_VOLATILITY, OPTION_IMPLIED_VOLATILITY, REBATE_RATE,
	FEE_RATE, YIELD_BID, YIELD_ASK, YIELD_BID_ASK, YIELD_LAST;
}
