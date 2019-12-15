package com.univocity.trader.notification;

import static com.univocity.trader.account.Order.Side.BUY;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.account.Client;
import com.univocity.trader.account.Order;
import com.univocity.trader.account.Trader;

public class OrderExecutionToLog implements OrderListener {
   private static final Logger log = LoggerFactory.getLogger(OrderExecutionToLog.class);

   @Override
   public void orderSubmitted(Order order, Trader trader, Client<?> client) {
      if (log.isDebugEnabled()) {
         SymbolPriceDetails f = trader.getPriceDetails();
         String type = StringUtils.rightPad(order.getSide().toString(), 8);
         String details = trader.getCandle().getFormattedCloseTimeWithYear() + " " + trader.getSymbol() + " " + type + " " + f.quantityToString(order.getQuantity()) + " @ $";
         if (order.getSide() == BUY) {
            details += f.priceToString(order.getPrice()) + " (Total spent: $" + f.priceToString(order.getTotalOrderAmount()) + ")";
         } else {
            details += f.priceToString(trader.getCandle().close) + " (" + trader.getFormattedPriceChangePct() + ") ";
            details += trader.exitReason();
            details += " >> " + trader.tradeLength() + " ticks >> [Min: $" + f.priceToString(trader.getMinPrice()) + " (" + trader.getFormattedMinChangePct() + ") - Max: $"
                  + f.priceToString(trader.getMaxPrice()) + " (" + trader.getFormattedMaxChangePct() + ")]";
            details += "\t Holdings ~$" + f.priceToString(trader.holdings()) + " " + trader.getReferenceCurrencySymbol();
         }
         log.debug(details);
      }
   }
}
