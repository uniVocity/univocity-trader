package com.univocity.trader.notification;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.indicators.base.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;

public class OrderExecutionToLog implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToLog.class);

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
			details += "0% filled" + time;
		} else {
			if (order.getStatus() != Order.Status.FILLED && order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
				details += order.getFormattedFillPct() + " filled, ";
			}
			details += "traded $" + f.priceToString(order.getTotalTraded());
			details += time;
		}
		return details;
	}

	private void logDetails(Order order, Trade trade, Client client) {
		if (log.isDebugEnabled()) {
			Trader trader = trade.trader();
			SymbolPriceDetails f = trader.priceDetails();
			String type = StringUtils.rightPad(order.getSide().toString(), 8);
			String details = trader.latestCandle().getFormattedCloseTimeWithYear() + " " + trader.symbol() + " " + type + " " + f.quantityToString(order.getQuantity()) + " @ $";
			if (order.isBuy()) {
				details += f.priceToString(order.getPrice());
				if (order.isFinalized()) {
					details += " + " + printFillDetails(order, trade, f);
				} else {
					details += " + PENDING     total committed: $" + f.priceToString(order.getTotalOrderAmount());
				}
			} else {
				details += f.priceToString(trader.latestCandle().close);
				if (order.isFinalized()) {
					details += " - " + printFillDetails(order, trade, f) + ", P/L: $" + f.priceToString(trade.actualProfitLoss()) + " [" + trade.formattedProfitLossPct() + "] ";
					details += " Holdings ~$" + f.priceToString(trader.holdings()) + " " + trader.referenceCurrencySymbol() + " (free: $" + f.priceToString(trader.balance("USDT").getFree()) + ")";
				} else {
					details += " - PENDING     order value: $" + f.priceToString(order.getTotalOrderAmount());
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
