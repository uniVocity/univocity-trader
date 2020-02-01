package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

/**
 * An immediate fill {@link OrderFillEmulator}.
 *
 * Orders will be always 100% filled using the closing price of the previous
 * candle.
 *
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ImmediateFillEmulator implements OrderFillEmulator {

	@Override
	public void fillOrder(DefaultOrder order, Candle candle) {
		if (!order.isFinalized()) {
			order.setStatus(Order.Status.FILLED);
			order.setExecutedQuantity(order.getQuantity());
		}
	}
}
