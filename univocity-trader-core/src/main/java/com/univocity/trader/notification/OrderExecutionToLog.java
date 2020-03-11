package com.univocity.trader.notification;

import com.univocity.trader.account.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

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

	private String printFillDetails(OrderExecutionLine o) {
		String details = StringUtils.rightPad(o.status.name(), 12);
		String time = " duration: " + o.duration;
		if (o.fillPct == 0.0) {
			details += "0.00% filled" + time;
		} else {
			if (o.fillPct != 100.0) {
				details += o.orderFillPercentage + " filled, ";
			}
			details += (o.isBuy ? "spent" : "sold") + " $" + o.valueTransacted + " " + o.fundSymbol;
			details += time;
		}
		return details;
	}

	private void logDetails(Order order, Trade trade, Client client) {
		if (log.isDebugEnabled()) {

			OrderExecutionLine o = new OrderExecutionLine(order, trade, trade.trader(), client);
			String type = StringUtils.rightPad(o.operation, 5);

			String quantity = o.quantity;
			if (maxQuantityLength < quantity.length()) {
				maxQuantityLength = quantity.length();
			}
			quantity = StringUtils.leftPad(quantity, maxQuantityLength);

			String price = o.price;
			if (maxPriceLength < price.length()) {
				maxPriceLength = price.length();
			}
			price = StringUtils.rightPad(price, maxPriceLength);

			String trigger = StringUtils.rightPad(o.trigger.shortName, 3);

			String details = StringUtils.rightPad(o.closeTime.toString(), 25) + " " + StringUtils.rightPad(o.assetSymbol, 8) + " " + trigger + type + " " + quantity + " @ $" + price;

//			details += "[" + order.getOrderId() + "]";
			if (order.isLongBuy() || order.isShortSell()) {
				if (order.isFinalized()) {
					details += " + " + printFillDetails(o) + ".";
					if (order.getExecutedQuantity() != 0) {
						if (order.isLongBuy()) {
							details += " Worth $" + o.price + " " + o.fundSymbol + " (free $" + o.freeBalance + " " + o.fundSymbol;
							if (!o.fundSymbol.equals(o.referenceCurrency)) {
								details += ", $" + o.freeBalanceReferenceCurrency + " " + o.referenceCurrency;
							}
							details += ")";
						} else if (order.isShortSell()) {
							details += " Shorting total " + o.assetSymbol + " " + o.shortedQuantity + " (" + o.fundSymbol + " margin reserve: $" + o.marginReserve + ", free $" + o.freeBalance + ")";
						}
					}
				} else {
					details += " + PENDING     worth $" + o.printOrderAmountAndCurrency();
				}
			} else {
				if (order.isFinalized()) {
					details += " - " + printFillDetails(o) + ".";
					if (o.fillPct > 0.0) {
						details += " P/L " + o.printReferenceCurrencyProfitLossAndChange();
					}

					details += " Holdings $" + o.printHoldingsAndReferenceCurrency() + " (free $" + o.freeBalanceReferenceCurrency + ")";
				} else {
					details += " - PENDING     worth $" + o.printOrderAmountAndCurrency();

					String pl = trade.formattedEstimateProfitLossPercentage(order);
					if (!pl.startsWith("-")) {
						pl = '+' + pl;
					}

					details += ". Expected P/L " + pl;

					if (trade.ticks() > 0) {
						details += " | " + trade.ticks() + " ticks [min " + o.printMinPriceAndChange() + ", max $" + o.printMaxPriceAndChange() + ")]";
					} else {
						details += " |";
					}
					details += " " + trade.exitReason();
				}
			}

			details = "[" + trade.id() + "]" + details;

			if (StringUtils.isNotBlank(client.getId())) {
				log.debug(client.getId() + ": " + details);
			} else {
				log.debug(details);
			}
		}
	}
}
