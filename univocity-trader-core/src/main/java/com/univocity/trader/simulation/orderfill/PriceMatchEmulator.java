package com.univocity.trader.simulation.orderfill;

import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Type.*;

/**
 * A price matching {@link OrderFillEmulator}.
 *
 * If the {@link Order.Type} is {@code LIMIT}, fills the order when its price is within the high/low of a future candle
 *
 * If the {@link Order.Type} is {@code MARKET}, fills the order using the average price of the next candle.
 * The average price depends on the {@link Order.Side}:
 *
 * <ul>
 *     <li>{@code BUY}: average = (open + close + high / 3)</li>
 *     <li>{@code SELL}: average = (open + close + low / 3)</li>
 * </ul>
 *
 * Orders will be always 100% filled once a matching candle is found.
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class PriceMatchEmulator implements OrderFillEmulator {

	@Override
	public void fillOrder(DefaultOrder order, Candle candle) {
		if (order.getType() == LIMIT) {
			if ((order.getSide() == BUY && order.getPrice() >= candle.low)
					|| (order.getSide() == SELL && order.getPrice() <= candle.high)) {
				order.setStatus(Order.Status.FILLED);
				order.setExecutedQuantity(order.getQuantity());

				double orderPrice = order.getPrice();
				if (order.isBuy() && candle.high < orderPrice) {
					order.setAveragePrice(candle.high);
				} else if (order.isSell() && candle.low > orderPrice) {
					order.setAveragePrice(candle.low);
				} else {
					order.setAveragePrice(order.getPrice());
				}
				order.setPartialFillDetails(order.getQuantity(), order.getAveragePrice());
			}
		} else if (order.getType() == MARKET) {
			order.setStatus(Order.Status.FILLED);
			order.setExecutedQuantity(order.getQuantity());
			if (order.getSide() == BUY) {
				order.setAveragePrice((candle.open + candle.close + candle.high) / 3.0);
			} else if (order.getSide() == SELL) {
				order.setAveragePrice((candle.open + candle.close + candle.low) / 3.0);
			}
			order.setPrice(order.getAveragePrice());
			order.setPartialFillDetails(order.getQuantity(), order.getAveragePrice());
		}
	}
}
