package com.univocity.trader.strategy;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.*;

/**
 * Class responsible for monitoring trades made by a {@link Trader}, after a {@link Strategy} returns a {@link Signal}. It can contain {@link Indicator}s to help deciding what to do on every tick
 * received from the {@link com.univocity.trader.Exchange}. Once a {@code BUY} is received, and no position is open, {@link #discardBuy(Strategy)} will be invoked to confirm if it is a good time to
 * buy. If it is, a position is open and {@link #bought()} will be called. After that, {@link #handleStop(Signal, Strategy)} will be invoked after every tick received from the exchange, to allow
 * closing the trade. If {@link #handleStop(Signal, Strategy)} returns a not {@code null} value or a {@code SELL} signal is received from the {@link Strategy}, {@link #allowExit()} will be invoked to
 * confirm whether to exit the trade. If the trade is closed, {@link #sold()} will be invoked. During the lifetime of the trade, a few other methods might be invoked: - {@link #highestProfit(double)}
 * when a winning trade reaches a new high positive rate of return. - {@link #worstLoss(double)} when a losing trade is reaches a new low negative rate of return. -
 * {@link #allowTradeSwitch(String, Candle, String)} when there are no funds available to trade anymore and a {@link Strategy} finds a buying opportunity, this method will be called to ask if the
 * current trade can be exited to release funds and open another trade in another instrument. Specific details about the trade can be obtained from the {@link Trader} instance.
 *
 * @see Trader
 * @see IndicatorGroup
 * @see Strategy
 */
public abstract class StrategyMonitor extends IndicatorGroup {

	/**
	 * The object responsible for creating and managing trades.
	 */
	protected Trader trader;

	/**
	 * Tests the current trade status to determine whether or not it should be exited. Will exit a trade regardless of any {@link Signal} emitted by a {@link Strategy}
	 *
	 * @param signal   the latest signal emitted by the given strategy
	 * @param strategy the strategy that originated the given signal
	 *
	 * @return {@code null} if the trade is to remain open or a {@code String} with a message indicating the reason for exiting the trade. This message will then be returned by
	 * {@link Trade#exitReason()} and can be included in logs or e-mails (as implemented in {@link com.univocity.trader.notification.OrderExecutionToEmail}).
	 */
	public String handleStop(Trade trade, Signal signal, Strategy strategy) {
		return null;
	}

	/**
	 * Checks if the {@code BUY} signal emitted by a given {@link Strategy} must be discarded, preventing the {@link Trader} to buy.
	 *
	 * @param strategy the strategy the emitted a {@code BUY} signal
	 *
	 * @return {@code false} if the {@link #trade} is allowed to buy into the instrument it is responsible for; {@code true} if the {@code BUY} signal should be ignored.
	 */
	public boolean discardBuy(Strategy strategy) {
		return false;
	}

	/**
	 * Indicates whether this monitor allows signals from multiple strategies, i.e. if more than one {@link Strategy} is being used, one can emit {@code BUY} signals and another {@code SELL} signals,
	 * and both will be accepted. If disallowed, once a {@link Strategy} enters a trade the signals emitted by another {@link Strategy} will be ignored.
	 *
	 * @return {@code false} if the {@link Strategy} that opens a trade must be the same {@link Strategy} that closes it, otherwise {@code true}
	 */
	public boolean allowMixedStrategies() {
		return true;
	}

	/**
	 * Notifies that the latest price movement reached its highest positive point so far.
	 *
	 * @param change the positive rate of return of the current trade (as a percentage value greater than 0.0 and in a scale of 100)
	 */
	public void highestProfit(Trade trade, double change) {
	}

	/**
	 * Notifies that the latest price movement reached its lowest negative point so far.
	 *
	 * @param change the negative rate of return of the current trade (as a percentage value between -100.0 and 0.0)
	 */
	public void worstLoss(Trade trade, double change) {
	}

	/**
	 * Notifies that the {@link #trade} bought some quantity of symbol {@link Trade#symbol()}
	 */
	public void bought(Trade trade, Order order) {
	}

	/**
	 * Notifies that any instruments of symbol {@link Trade#symbol()} held by the {@link #trade} were sold.
	 */
	public void sold(Trade trade, Order order) {
	}

	/**
	 * Confirms that the current trade can be closed.
	 *
	 * @return {@code true} if the current trade can be exited, otherwise {@code false}
	 */
	public boolean allowExit(Trade trade) {
		return true;
	}

	/**
	 * Checks if the current open position can be closed to release funds for another trade in another instrument to be opened.
	 *
	 * @param exitSymbol   the symbol of the instrument to be bought in case the current open trade can be closed (e.g. BTC, USD).
	 * @param candle       the latest candle of the exit symbol ticker
	 * @param candleTicker the full ticker of the given candle (e.g. BTCETH, EURUSD)
	 *
	 * @return a flag indicating whether or not the current trade can be exited and the position reallocated to the given exit symbol.
	 */
	public boolean allowTradeSwitch(Trade trade, String exitSymbol, Candle candle, String candleTicker) {
		return false;
	}

	/**
	 * Assigns the trade responsible for managing orders in a position to this monitor.
	 *
	 * @param trader the object responsible for all trading decisions performed against an instrument.
	 */
	public void setTrader(Trader trader) {
		this.trader = trader;
	}
}
