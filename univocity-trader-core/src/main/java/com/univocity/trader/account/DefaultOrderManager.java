package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.slf4j.*;

import java.math.*;
import java.util.function.*;

public class DefaultOrderManager implements OrderManager {

	private static final Logger log = LoggerFactory.getLogger(DefaultOrderManager.class);
	private final TimeInterval maxTimeToKeepOrderOpen;

	public DefaultOrderManager() {
		this(TimeInterval.minutes(10));
	}

	public DefaultOrderManager(TimeInterval maxTimeToKeepOrderOpen) {
		this.maxTimeToKeepOrderOpen = maxTimeToKeepOrderOpen;
	}

	@Override
	public void prepareOrder(SymbolPriceDetails priceDetails, OrderBook book, OrderRequest order, Candle latestCandle) {
		BigDecimal originalPrice = order.getPrice();

		double availableQuantity = order.getQuantity().doubleValue();

		if (book != null) {
			double spread = book.getSpread(availableQuantity);
			double ask = book.getAverageAskAmount(availableQuantity);
			double bid = book.getAverageBidAmount(availableQuantity);

			// aims price at central price point of the spread.
			if (order.getSide() == Order.Side.BUY) {
				order.setPrice(BigDecimal.valueOf(bid + (spread / 2.0)));
			} else {
				order.setPrice(BigDecimal.valueOf(ask - (spread / 2.0)));
			}

			log.debug("{} - spread of {}: Ask {}, Bid {}. Closed at {}. Going to {} at ${}.", order.getSymbol(),
					priceDetails.priceToString(spread), priceDetails.priceToString(ask),
					priceDetails.priceToString(bid), priceDetails.priceToString(originalPrice), order.getSide(),
					priceDetails.priceToString(order.getPrice()));
		}
	}

	@Override
	public void finalized(Order order, Trader trader) {
//		System.out.println(order.print(trader.getCandle().closeTime));
	}

	@Override
	public void updated(Order order, Trader trader, Consumer<Order> resubmission) {

	}

	@Override
	public void unchanged(Order order, Trader trader, Consumer<Order> resubmission) {
		if (order.getTimeElapsed(trader.latestCandle().closeTime) >= maxTimeToKeepOrderOpen.ms) {
			order.cancel();
		}
	}

	@Override
	public boolean cancelToReleaseFundsFor(Order order, Trader currentTrader, Trader newSymbolTrader) {
		if (order.getTimeElapsed(currentTrader.latestCandle().closeTime) > maxTimeToKeepOrderOpen.ms / 2) {
			order.cancel();
			return true;
		}
		return false;
	}
}
