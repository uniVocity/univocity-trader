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
			details += (order.isBuy() ? "spent" : "sold") + " $" + f.priceToString(order.getTotalTraded());
			details += time;
		}
		return details;
	}

	private void logDetails(Order order, Trade trade, Client client) {
		if (log.isDebugEnabled()) {
			Trader trader = trade.trader();
			SymbolPriceDetails f = trader.priceDetails();
			SymbolPriceDetails rf = trader.referencePriceDetails();
			String currency = " " + trader.referenceCurrencySymbol();
			String type = StringUtils.rightPad(order.sideDescription(), 4);

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
			details += StringUtils.rightPad(trader.assetSymbol(), 8) + " " + type + " " + quantity + " @ $" + price;
//			details += "[" + order.getOrderId() + "]";
			if (order.isLongBuy() || order.isShortSell()) {
				if (order.isFinalized()) {
					details += " + " + printFillDetails(order, trade, rf) + ".";
					if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) != 0) {
						details += " Worth $" + rf.priceToString(order.getExecutedQuantity().doubleValue() * trader.lastClosingPrice()) + currency + " (free $" + rf.priceToString(trader.balance().getFree()) + ")";
					}
				} else {
					details += " + PENDING     committed $" + rf.priceToString(order.getTotalOrderAmount()) + currency;
				}
			} else {
				if (order.isFinalized()) {
					details += " - " + printFillDetails(order, trade, rf) + "." + (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) == 0 ? "" : " P/L $" + rf.priceToString(trade.actualProfitLoss()) + " [" + trade.formattedProfitLossPct() + "].");
					details += " Holdings $" + rf.priceToString(trader.holdings()) + currency + " (free $" + rf.priceToString(trader.balance().getFree()) + ")";
				} else {
					details += " - PENDING     worth $" + rf.priceToString(order.getTotalOrderAmount()) + currency;

					String pl = trade.formattedEstimateProfitLossPercentage(order);
					if (!pl.startsWith("-")) {
						pl = '+' + pl;
					}

					details += ". Expected P/L " + pl;

					details += " | " + trade.ticks() + " ticks [min " + trade.formattedMinPriceAndPercentage() + ", max $" + trade.formattedMaxPriceAndPercentage() + ")]";
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
