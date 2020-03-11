package com.univocity.trader.examples;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;

import static com.univocity.trader.account.Order.Type.*;

/**
 * @author uniVocity Software Pty Ltd - <a href="mailto:dev@univocity.com">dev@univocity.com</a>
 */
public class ExampleOrderManager extends DefaultOrderManager {

	@Override
	public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
		if (order.isBuy() && order.isLong() || order.isSell() && order.isShort()) {
			//attached orders are created with opposite side. If parent order is BUY, the following orders will be SELL orders, and vice versa.
			OrderRequest marketSellOnLoss = order.attach(MARKET, -2.0);
			OrderRequest takeProfit = order.attach(MARKET, 4.0);
		}
	}
}
