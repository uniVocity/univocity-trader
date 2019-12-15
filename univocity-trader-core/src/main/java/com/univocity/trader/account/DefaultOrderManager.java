package com.univocity.trader.account;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.*;
import org.slf4j.*;

import java.math.*;

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
		if (book == null) {
			return;
		}
		BigDecimal originalPrice = order.getPrice();

		double availableQuantity = order.getQuantity().doubleValue();

		double spread = book.getSpread(availableQuantity);
		double ask = book.getAverageAskAmount(availableQuantity);
		double bid = book.getAverageBidAmount(availableQuantity);

		//aims price at central price point of the spread.
		if (order.getSide() == Order.Side.BUY) {
			order.setPrice(new BigDecimal(bid + (spread / 2.0)));
		} else {
			order.setPrice(new BigDecimal(ask - (spread / 2.0)));
		}

		log.debug("{} - spread of {}: Ask {}, Bid {}. Closed at {}. Going to {} at ${}.",
				order.getSymbol(),
				priceDetails.priceToString(spread),
				priceDetails.priceToString(ask),
				priceDetails.priceToString(bid),
				priceDetails.priceToString(originalPrice),
				order.getSide(),
				priceDetails.priceToString(order.getPrice())
		);
	}

	@Override
	public void finalized(Order order) {

	}

	@Override
	public void updated(Order order) {

	}

	@Override
	public void unchanged(Order order) {
		if (order.getTimeElapsed() >= maxTimeToKeepOrderOpen.ms) {
			order.cancel();
			return;
		}
	}

	@Override
	public boolean cancelToReleaseFundsFor(Order order, Trader trader) {
		if (order.getTimeElapsed() > maxTimeToKeepOrderOpen.ms / 2) {
			order.cancel();
			return true;
		}
		return false;
	}
}
