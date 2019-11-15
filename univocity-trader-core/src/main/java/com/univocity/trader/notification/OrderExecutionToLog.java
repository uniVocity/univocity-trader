package com.univocity.trader.notification;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import static com.univocity.trader.account.Order.Side.*;

public class OrderExecutionToLog implements OrderEventListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToLog.class);

	@Override
	public void onOrderUpdate(Order order, Trader trader, Client client) {
		if (log.isDebugEnabled()) {
			SymbolPriceDetails f = trader.getPriceDetails();
			String type = StringUtils.rightPad(order.getSide().toString(), 8);
			String details = trader.getCandle().getFormattedCloseTimeWithYear() + " " + trader.getSymbol() + " " + type + " " + trader.getLastClosingPrice() + " @ $";
			if (order.getSide() == BUY) {
				details += f.priceToString(order.getPrice()) + " (" + f.quantityToString(order.getQuantity()) + " units. Total spent: $" + f.priceToString(order.getTotalSpent()) + ")";
			} else {
				details += trader.getCandle().close + " (" + trader.getFormattedPriceChangePct() + ") ";
				details += trader.exitReason();
				details += " >> " + trader.tradeLength() + " ticks >> [Min: $" + f.priceToString(trader.getMinPrice()) + " (" + trader.getFormattedMinChangePct() + ") - Max: $" + f.priceToString(trader.getMaxPrice()) + " (" + trader.getFormattedMaxChangePct() + ")]";
				details += "\t Holdings ~$" + f.priceToString(trader.holdings()) + " " + trader.getReferenceCurrencySymbol();
			}
			log.debug(details);
		}
	}
}
