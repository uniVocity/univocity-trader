package com.univocity.trader.notification;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.indicators.base.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;

public class OrderExecutionToLog implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToLog.class);
	private int maxQuantityLength;
	private int maxPriceLength;

	@Override
	public void orderSubmitted(Order order, Trade trade, Client client) {
		logDetails(order, trade, client);
	}

	@Override
	public void orderFinalized(Order order, Trade trade, Client client) {
		logDetails(order, trade, client);
	}

	private String printFillDetails(Order order, Trade trade, SymbolPriceDetails f) {
		String details = StringUtils.rightPad(order.getStatus().name(), 12);
		String time = " after " + TimeInterval.getFormattedDuration(order.getTimeElapsed(trade.latestCandle().closeTime));
		if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) == 0) {
			details += "0.00% filled" + time;
		} else {
			if (order.getStatus() != Order.Status.FILLED && order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
				details += order.getFormattedFillPct() + " filled, ";
			}
			details += (order.isBuy() ? "spent " : "sold ") + "$ " + f.priceToString(order.getTotalTraded());
			details += time;
		}
		return details;
	}

	private void logDetails(Order order, Trade trade, Client client) {
		if (log.isDebugEnabled()) {
			Trader trader = trade.trader();
			SymbolPriceDetails f = trader.priceDetails();
			SymbolPriceDetails rf = trader.referencePriceDetails();
			String type = StringUtils.rightPad(order.getSide().toString(), 4);

			String quantity = f.quantityToString(order.getQuantity());
			if (maxQuantityLength < quantity.length()) {
				maxQuantityLength = quantity.length();
			}
			quantity = StringUtils.leftPad(quantity, maxQuantityLength);

			String price = order.isBuy() ? f.priceToString(order.getPrice()) : f.priceToString(trader.latestCandle().close);
			if (maxPriceLength < price.length()) {
				maxPriceLength = price.length();
			}
			price = StringUtils.rightPad(price, maxPriceLength);

			String details = trader.latestCandle().getFormattedCloseTime("yy-MM-dd HH:mm") + " ";
			details += trader.assetSymbol() + " " + type + " " + quantity + " @ $" + price;
			if (order.isBuy()) {
				if (order.isFinalized()) {
					details += " + " + printFillDetails(order, trade, rf);
				} else {
					details += " + PENDING     total committed: $" + rf.priceToString(order.getTotalOrderAmount()) + " " + trader.referenceCurrencySymbol();
				}

				if (order.isFinalized()) {
					details += ". Worth ~$" + rf.priceToString(trader.assetQuantity() * trader.lastClosingPrice()) + " " + trader.referenceCurrencySymbol() + " (free: $" + rf.priceToString(trader.balance().getFree()) + ")";
				}
			} else {
				if (order.isFinalized()) {
					details += " - " + printFillDetails(order, trade, rf) + ", P/L: $" + rf.priceToString(trade.actualProfitLoss()) + " [" + trade.formattedProfitLossPct() + "] ";
					details += " Holdings ~$" + rf.priceToString(trader.holdings()) + " " + trader.referenceCurrencySymbol() + " (free: $" + rf.priceToString(trader.balance().getFree()) + ")";
				} else {
					details += " - PENDING     order value: $" + rf.priceToString(order.getTotalOrderAmount()) + " " + trader.referenceCurrencySymbol();
					details += " expected returns: " + trade.formattedEstimateProfitLossPercentage(order) + " ";
					details += " >> " + trade.tradeLength() + " ticks >> [Min: $" + f.priceToString(trade.minPrice()) + " (" + trade.formattedMinChangePct() + ") - Max: $" + f.priceToString(trade.maxPrice()) + " (" + trade.formattedMaxChangePct() + ")]";
					details += " " + trade.exitReason();
				}
			}
			if (StringUtils.isNotBlank(client.getId())) {
				log.debug(client.getId() + ": " + details);
			} else {
				log.debug(details);
			}
		}
	}
}
