package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.simulation.*;

/**
 * Controls how an {@link Order} should be filled
 * in {@link SimulatedClientAccount#updateOpenOrders(String, Candle)} when running a simulation.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public interface OrderFillEmulator {

	/**
	 * Attempts to fill the given order using the information of a candle
	 *
	 * @param order  the order to fill
	 * @param candle the latest candle received from history.
	 */
	void fillOrder(Order order, Candle candle);
}
