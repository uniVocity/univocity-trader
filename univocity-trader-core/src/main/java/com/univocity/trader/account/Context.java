package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;
import com.univocity.trader.simulation.*;
import com.univocity.trader.strategy.*;

public class Context {

	final String symbol;
	final SymbolPriceDetails priceDetails;
	Candle latestCandle;
	Trader trader;
	Trade trade;
	Strategy strategy;
	Signal signal;
	StrategyMonitor strategyMonitor;
	String exitReason;
	Parameters parameters;

	public Context(TradingManager tradingManager, Parameters parameters) {
		this.symbol = tradingManager.getSymbol();
		this.priceDetails = tradingManager.getPriceDetails();
		this.parameters = parameters;
	}

	public final String symbol() {
		return symbol;
	}

	/**
	 * Price details associated with the symbol of the given order request, which includes number of decimal digits to use
	 * and minimum order quantity. Note that after this method executes, the order price and amount will be adjusted to conform
	 * to the given price details. If no price details exist, this parameter will be set to {@code SymbolPriceDetails.NOOP}.
	 *
	 * @return price details of the current symbol
	 */
	public final SymbolPriceDetails priceDetails() {
		return priceDetails;
	}

	/**
	 * The {@link Trader} object responsible for executing trades on a given symbol. From there you can obtain information such as the
	 * latest candle received from the exchange (e.g. {@link Trader#latestCandle()}) and how much is your account worth {using @link
	 * Trader#holdings()} to help you better determine the size of your order.
	 *
	 * @return the trader originating order requests.
	 */
	public final Trader trader() {
		return trader;
	}

	/**
	 * Provides the optional parameter set used for parameter optimization which is passed on to the
	 * {@link StrategyMonitor} instances created by the given monitorProvider
	 *
	 * @return the trader parameters currently in use
	 */
	public final Parameters parameters() {
		return parameters;
	}

	/**
	 * The current {@link Trade} whose position being increased or closed. Will be {@code null} if this is a new trade
	 *
	 * @return the current trade if it exists.
	 */
	public final Trade trade() {
		return trade;
	}

	public final Strategy strategy() {
		return strategy;
	}

	public final Signal signal() {
		return signal;
	}

	public final StrategyMonitor monitor() {
		return strategyMonitor;
	}

	public final String exitReason() {
		return exitReason != null ? exitReason : trade != null ? trade.exitReason() : null;
	}

	public final void latestCandle(Candle latestCandle) {
		if (latestCandle != null) {
			this.latestCandle = latestCandle;
		}
	}

	public final Candle latestCandle() {
		return latestCandle;
	}
}
