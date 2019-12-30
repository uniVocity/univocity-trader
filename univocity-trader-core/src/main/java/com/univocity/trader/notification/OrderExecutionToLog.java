package com.univocity.trader.notification;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.indicators.base.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;

import static com.univocity.trader.account.Order.Side.*;

public class OrderExecutionToLog implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToLog.class);

	@Override
	public void orderSubmitted(Order order, Trader trader, Client client) {
		logDetails(order, trader, client);
	}

	@Override
	public void orderFinalized(Order order, Trader trader, Client client) {
		logDetails(order, trader, client);
	}

	private String printFillDetails(Order order, SymbolPriceDetails f, Trader trader) {
		String details = StringUtils.rightPad(order.getStatus().name(), 12);
		String time = " after " + TimeInterval.getFormattedDuration(order.getTimeElapsed(trader.latestCandle().closeTime));
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

	private void logDetails(Order order, Trader trader, Client client) {
		if (log.isDebugEnabled()) {
			SymbolPriceDetails f = trader.priceDetails();
			String type = StringUtils.rightPad(order.getSide().toString(), 8);
			String details = trader.latestCandle().getFormattedCloseTimeWithYear() + " " + trader.symbol() + " " + type + " " + f.quantityToString(order.getQuantity()) + " @ $";
			if (order.isBuy()) {
				details += f.priceToString(order.getPrice());
				if (order.isFinalized()) {
					details += " + " + printFillDetails(order, f, trader);
				} else {
					details += " + PENDING     total committed: $" + f.priceToString(order.getTotalOrderAmount());
				}
			} else {
				details += f.priceToString(trader.latestCandle().close);
				if (order.isFinalized()) {
					details += " - " + printFillDetails(order, f, trader) + ", P/L: $" + f.priceToString(trader.actualProfitLoss()) + " [" + trader.formattedProfitLossPct() + "] ";
					details += " Holdings ~$" + f.priceToString(trader.holdings()) + " " + trader.referenceCurrencySymbol() + " (free: $" + f.priceToString(trader.balance("USDT").getFree()) + ")";
				} else {
					details += " - PENDING     order value: $" + f.priceToString(order.getTotalOrderAmount());
					details += " expected returns: " + trader.formattedEstimateProfitLossPercentage(order) + " ";
					details += " >> " + trader.tradeLength() + " ticks >> [Min: $" + f.priceToString(trader.minPrice()) + " (" + trader.formattedMinChangePct() + ") - Max: $" + f.priceToString(trader.maxPrice()) + " (" + trader.formattedMaxChangePct() + ")]";
					details += " " + trader.exitReason();
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
