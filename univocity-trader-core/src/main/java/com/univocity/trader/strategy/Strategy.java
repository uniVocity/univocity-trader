package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.indicators.*;

/**
 * A {@code Strategy} is responsible for processing price updates transmitted by an {@link com.univocity.trader.Exchange}
 * and generating a {@link Signal} which is then submitted to {@link Trader#trade(Candle, Signal, Strategy)}
 * to open, maintain or close a trade.
 *
 * {@code Strategy} implementations that make use of any {@link Indicator} implementation in package {@link com.univocity.trader.indicators}
 * should ideally extend from {@link IndicatorStrategy} for proper initialization.
 *
 * @see Signal
 * @see Trader
 * @see Indicator
 * @see IndicatorStrategy
 */
public interface Strategy {

	/**
	 * Processes the latest price update of a symbol to produce a {@link Signal}
	 *
	 * @param candle the latest candle received from a live {@link com.univocity.trader.Exchange} or the trading history of a symbol
	 *               (typically managed by {@link com.univocity.trader.simulation.SimulatedExchange}).
	 *
	 * @param context
	 *
	 * @return an indication to {@code BUY}, {@code SELL} or do nothing (i.e. {@code NEUTRAL}). Any other value will be ignored by the
	 * {@link com.univocity.trader.account.Trader} that processes this {@link Signal}.
	 */
	Signal getSignal(Candle candle, Context context);

	/**
	 * The {@link Trade.Side} side ({@code LONG}, {@code SHORT} or both) this strategy applies to.
	 * When {@link #getSignal(Candle, Context)} produces a {@code BUY}, and the trade side returned by this method is {@code null},
	 * the {@link Trader} working with the instrument being traded will try to exit any opened short positions and
	 * will also attempt to go long on the given instrument. If the signal is {@code SELL}, the trader will try to
	 * close any open long positions, and sell the current instrument short.
	 *
	 * If the signals produced by this strategy only apply to {@code LONG} or {@code SHORT} positions, the {@code tradeSide}
	 * method must return which side to work with.
	 *
	 * This method is only relevant if shorting is enabled in the account (i.e. {@link AccountConfiguration#shortingEnabled()}).
	 *
	 * @return the trade side of this strategy, {@code null} if both can be considered.
	 */
	default Trade.Side tradeSide() {
		return null;
	}

	default boolean exitOnOppositeSignal(){
		return true;
	}
}
