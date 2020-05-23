package com.univocity.trader.examples;

import com.univocity.trader.account.*;

import static com.univocity.trader.account.Order.Type.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ExampleOrderManager extends DefaultOrderManager {

	@Override
	public void prepareOrder(OrderBook book, OrderRequest order, Context context) {
		if (order.isBuy() && order.isLong() || order.isSell() && order.isShort()) {
			//attached orders are created with opposite side. If parent order is BUY, the following orders will be SELL orders, and vice versa.
			OrderRequest marketSellOnLoss = order.attachToPercentageChange(MARKET, -2.0);
			OrderRequest takeProfit = order.attachToPercentageChange(MARKET, 4.0);
		}
	}
}
