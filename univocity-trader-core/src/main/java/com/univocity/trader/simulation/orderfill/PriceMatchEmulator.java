package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import java.math.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Type.*;

/**
 * A price matching {@link OrderFillEmulator}.
 *
 * If the {@link Order.Type} is {@code LIMIT}, fills the order when its price is
 * within the high/low of a future candle
 *
 * If the {@link Order.Type} is {@code MARKET}, fills the order using the
 * average price of the next candle. The average price depends on the
 * {@link Order.Side}:
 *
 * <ul>
 * <li>{@code BUY}: average = (open + close + high / 3)</li>
 * <li>{@code SELL}: average = (open + close + low / 3)</li>
 * </ul>
 *
 * Orders will be always 100% filled once a matching candle is found.
 *
 * @author uniVocity Software Pty Ltd -
 *         <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class PriceMatchEmulator implements OrderFillEmulator {

	@Override
	public void fillOrder(DefaultOrder order, Candle candle) {
		if (order.getType() == LIMIT) {
			if ((order.getSide() == BUY && order.getPrice().compareTo(round(BigDecimal.valueOf(candle.low))) >= 0)
					|| (order.getSide() == SELL
							&& order.getPrice().compareTo(round(BigDecimal.valueOf(candle.high))) <= 0)) {
				order.setStatus(Order.Status.FILLED);
				order.setExecutedQuantity(order.getQuantity());
			}
		} else if (order.getType() == MARKET) {
			order.setStatus(Order.Status.FILLED);
			order.setExecutedQuantity(order.getQuantity());
			if (order.getSide() == BUY) {
				order.setPrice(BigDecimal.valueOf((candle.open + candle.close + candle.high) / 3.0));
			} else if (order.getSide() == SELL) {
				order.setPrice(BigDecimal.valueOf((candle.open + candle.close + candle.low) / 3.0));
			}
		}
	}
}
