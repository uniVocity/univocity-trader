package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

/**
 * An {@link OrderFillEmulator} that never fills the order.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public final class NoFillEmulator implements OrderFillEmulator {

	@Override
	public void fillOrder(Order order, Candle candle) {
	}
}
