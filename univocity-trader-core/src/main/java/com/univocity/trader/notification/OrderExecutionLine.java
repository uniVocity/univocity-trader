package com.univocity.trader.notification;

import com.univocity.parsers.annotations.*;
import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.indicators.base.*;

import java.sql.*;

public class OrderExecutionLine {

	@Parsed
	Timestamp closeTime;
	@Parsed
	String clientId;
	@Parsed
	String symbol;
	@Parsed
	String operation;
	@Parsed
	String exitReason;
	@Parsed
	int ticks;
	@Parsed
	String currency;
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

	public OrderExecutionLine(Order order, Trade trade, Client client) {
		Trader trader = trade.trader();

		SymbolPriceDetails priceDetails = trader.priceDetails();
		SymbolPriceDetails refPriceDetails = trader.referencePriceDetails();

		currency = order.getFundsSymbol();
		quantity = priceDetails.quantityToString(order.getQuantity());
		price = order.isBuy() ? priceDetails.priceToString(order.getPrice()) : priceDetails.priceToString(trader.latestCandle().close);
		closeTime = trader.latestCandle().closeTimestamp();
		symbol = trader.assetSymbol();
		referenceCurrency = trader.referenceCurrencySymbol();
		status = order.getStatus();
		operation = order.sideDescription();
		orderType = order.getType();
		executedQuantity = priceDetails.quantityToString(order.getExecutedQuantity());
		valueTransacted = priceDetails.priceToString(order.getTotalTraded());
		orderAmount = priceDetails.priceToString(order.getTotalOrderAmount());
		priceChangePct = trade.formattedPriceChangePct();
		Balance balance = trader.balanceOf(currency);
		freeBalance = priceDetails.priceToString(balance.getFree());
		shortedQuantity = priceDetails.quantityToString(trader.balance().getShortedAmount());
		marginReserve = priceDetails.priceToString(balance.getMarginReserve(order.getAssetsSymbol()));

		freeBalanceReferenceCurrency = refPriceDetails.priceToString(trader.balance().getFree());
		profitLoss = priceDetails.priceToString(trade.actualProfitLoss());
		profitLossPct = trade.formattedProfitLossPct();
		profitLossReferenceCurrency = refPriceDetails.priceToString(trade.actualProfitLossInReferenceCurrency());
		estimatedProfitLossPct = trade.formattedEstimateProfitLossPercentage(order);
		holdings = refPriceDetails.priceToString(trader.holdings());
		minChangePct = trade.formattedMinChangePct();
		maxChangePct = trade.formattedMaxChangePct();
		minPrice = priceDetails.priceToString(trade.minPrice());
		maxPrice = priceDetails.priceToString(trade.maxPrice());
		ticks = trade.ticks();
		exitReason = trade.exitReason();
		clientId = client.getId();
		duration = TimeInterval.getFormattedDurationShort(order.getTimeElapsed(trade.latestCandle().closeTime));
		orderFillPercentage = order.getFormattedFillPct();
	}
}




