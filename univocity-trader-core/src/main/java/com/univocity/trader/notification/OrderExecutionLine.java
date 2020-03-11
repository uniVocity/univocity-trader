package com.univocity.trader.notification;

import com.univocity.parsers.annotations.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.indicators.base.*;

import java.sql.*;

public class OrderExecutionLine {

	@Parsed
	long tradeId;
	@Parsed
	String orderId;
	@Parsed
	Timestamp closeTime;
	@Parsed
	String clientId;
	@Parsed
	String assetSymbol;
	@Parsed
	String operation;
	@Parsed
	String exitReason;
	@Parsed
	Integer ticks;
	@Parsed
	String fundSymbol;
	@Parsed
	String minPrice;
	@Parsed
	String minChangePct;
	@Parsed
	String maxChangePct;
	@Parsed
	String maxPrice;
	@Parsed
	String price;
	@Parsed
	String averagePrice;
	@Parsed
	String priceChangePct;
	@Parsed
	String quantity;
	@Parsed
	String referenceCurrency;
	@Parsed
	Order.Status status;
	@Parsed
	Order.Type orderType;
	@Parsed
	String orderAmount;
	@Parsed
	String executedQuantity;
	@Parsed
	String valueTransacted;
	@Parsed
	String freeBalance;
	@Parsed
	String shortedQuantity;
	@Parsed
	String marginReserve;
	@Parsed
	String freeBalanceReferenceCurrency;
	@Parsed
	String estimatedProfitLossPct;
	@Parsed
	String profitLoss;
	@Parsed
	String profitLossPct;
	@Parsed
	String profitLossReferenceCurrency;
	@Parsed
	String holdings;
	@Parsed
	String duration;
	@Parsed
	String orderFillPercentage;
	@Parsed
	Order.TriggerCondition trigger;
	@Parsed
	String triggerPrice;

	double fillPct;
	boolean isShort;
	boolean isBuy;

	public OrderExecutionLine(Order order, Trade trade, Trader trader, Client client) {
		SymbolPriceDetails priceDetails = trader.priceDetails();
		SymbolPriceDetails refPriceDetails = trader.referencePriceDetails();

		clientId = client.getId();
		referenceCurrency = trader.referenceCurrencySymbol();

		freeBalanceReferenceCurrency = refPriceDetails.priceToString(trader.balance().getFree());
		holdings = refPriceDetails.priceToString(trader.holdings());

		double priceAmount = order == null || order.getPrice() == 0.0 ? trader.latestCandle().close : order.getPrice();

		if (order != null) {
			price = priceDetails.priceToString(priceAmount);
			fundSymbol = trader.fundSymbol();
			closeTime = trader.latestCandle().closeTimestamp();
			assetSymbol = trader.assetSymbol();

			Balance balance = trader.balanceOf(fundSymbol);
			shortedQuantity = priceDetails.quantityToString(trader.balance(assetSymbol).getShorted());

			SymbolPriceDetails amountDetails = fundSymbol.equals(referenceCurrency) ? refPriceDetails : priceDetails;

			freeBalance = amountDetails.priceToString(balance.getFree());
			marginReserve = amountDetails.priceToString(balance.getMarginReserve(trader.assetSymbol()));
			valueTransacted = amountDetails.priceToString(order.getTotalTraded());
			price = order.isBuy() ? amountDetails.priceToString(order.getPrice()) : price;
			averagePrice = amountDetails.priceToString(order.getAveragePrice());

			orderId = order.getOrderId();
			quantity = priceDetails.quantityToString(order.getQuantity());
			status = order.getStatus();
			operation = order.sideDescription();
			isShort = order.isShort();
			isBuy = order.isBuy();
			orderType = order.getType();
			executedQuantity = priceDetails.quantityToString(order.getExecutedQuantity());
			fillPct = order.getFillPct();
			orderFillPercentage = order.getFormattedFillPct();

			if (orderType == Order.Type.MARKET && fillPct == 0.0) {
				orderAmount = amountDetails.priceToString(order.getQuantity() * trader.lastClosingPrice());
			} else {
				orderAmount = amountDetails.priceToString(order.getTotalOrderAmount());
			}
			this.trigger = order.getTriggerCondition();
			this.triggerPrice = priceDetails.priceToString(order.getTriggerPrice());
		} else {
			operation = "END";
		}
		if (trade != null) {
			tradeId = trade.id();
			priceChangePct = trade.formattedPriceChangePct();
			if (trade.isFinalized()) {
				profitLoss = priceDetails.priceToString(trade.actualProfitLoss());
				profitLossPct = trade.formattedProfitLossPct();
				profitLossReferenceCurrency = refPriceDetails.priceToString(trade.actualProfitLossInReferenceCurrency());
			}
			minChangePct = trade.formattedMinChangePct();
			maxChangePct = trade.formattedMaxChangePct();
			minPrice = priceDetails.priceToString(trade.minPrice());
			maxPrice = priceDetails.priceToString(trade.maxPrice());
			ticks = trade.ticks();
			exitReason = trade.exitReason();

			if (order != null) {
				estimatedProfitLossPct = trade.formattedEstimateProfitLossPercentage(order);
				duration = TimeInterval.getFormattedDurationShort(order.getTimeElapsed(trade.latestCandle().closeTime));
			}
		}
	}

	public String printMinChange() {
		return isShort ? maxChangePct : minChangePct;
	}

	public String printMaxChange() {
		return isShort ? minChangePct : maxChangePct;
	}

	public String printMaxPriceAndChange() {
		return concat(maxPrice, printMaxChange());
	}

	public String printMinPriceAndChange() {
		return concat(maxPrice, printMinChange());
	}

	public String printLastPriceAndChange() {
		return concat(price, priceChangePct);
	}

	public String printProfitLossAndChange() {
		return concat(profitLoss, profitLossPct);
	}

	public String printReferenceCurrencyProfitLossAndChange() {
		return referenceCurrency + " " + concat(profitLossReferenceCurrency, profitLossPct);
	}

	public String printHoldingsAndReferenceCurrency() {
		return holdings + " " + referenceCurrency;
	}

	public String printOrderAmountAndCurrency() {
		return orderAmount + " " + fundSymbol;
	}

	private String concat(String amount, String percentage) {
		return amount + " (" + percentage + ')';
	}
}
