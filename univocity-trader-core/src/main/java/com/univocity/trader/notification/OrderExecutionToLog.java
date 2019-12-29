package com.univocity.trader.notification;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
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

	private void logDetails(Order order, Trader trader, Client client) {
		if (log.isDebugEnabled()) {
			SymbolPriceDetails f = trader.priceDetails();
			String type = StringUtils.rightPad(order.getSide().toString(), 8);
			String details = trader.latestCandle().getFormattedCloseTimeWithYear() + " " + trader.symbol() + " " + type + " " + f.quantityToString(order.getQuantity()) + " @ $";
			if (order.getSide() == BUY) {
				details += f.priceToString(order.getPrice());
				if (order.isFinalized()) {
					details += " (" + order.getStatus();
					if (order.getExecutedQuantity().compareTo(BigDecimal.ZERO) == 0) {
						details += " 0% filled.";
					} else {
						if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
							details += " " + order.getFormattedFillPct() + " filled";
						}
						details += " - spent: $" + f.priceToString(order.getTotalTraded()) + ")";
					}

				} else {
					details += " (PENDING - total committed: $" + f.priceToString(order.getTotalOrderAmount()) + ")";
				}
			} else {
				details += f.priceToString(trader.latestCandle().close);
				if (order.isFinalized()) {
					if (trader.position().isEmpty()) {
						details += " (" + order.getStatus() + ", P/L: " + f.priceToString(trader.actualProfitLoss()) + " [" + trader.formattedProfitLossPct() + "]) ";
					} else {
						details += " (PARTIAL EXIT - " + order.getStatus() + ", P/L: " + f.priceToString(trader.actualProfitLoss()) + " [" + trader.formattedProfitLossPct() + "]) ";
					}
					details += "\t Holdings ~$" + f.priceToString(trader.holdings()) + " " + trader.referenceCurrencySymbol();
				} else {
					details += " (PENDING - expected returns: " + trader.formattedPriceChangePct() + ") ";
					details += " >> " + trader.tradeLength() + " ticks >> [Min: $" + f.priceToString(trader.minPrice()) + " (" + trader.formattedMinChangePct() + ") - Max: $" + f.priceToString(trader.maxPrice()) + " (" + trader.formattedMaxChangePct() + ")]";
					details += trader.exitReason();
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
