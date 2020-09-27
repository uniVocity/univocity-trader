package com.univocity.trader.notification;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.account.*;
import com.univocity.trader.indicators.base.TimeInterval;

import java.sql.Timestamp;

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

	double minPrice;
	double minChangePct;
	double maxChangePct;
	double maxPrice;
	double price;
	double averagePrice;
	@Parsed
	String priceChangePct;
	double quantity;
	@Parsed
	String referenceCurrency;
	@Parsed
	Order.Status status;
	@Parsed
	Order.Type orderType;
	double orderAmount;
	double executedQuantity;
	double valueTransacted;
	double freeBalance;
	double shortedQuantity;
	double marginReserve;
	Double freeBalanceReferenceCurrency;
	@Parsed
	String estimatedProfitLossPct;
	@Parsed
	Double profitLoss;
	@Parsed
	String profitLossPct;
	@Parsed
	Double profitLossReferenceCurrency;
	@Parsed
	String holdings;
	@Parsed
	String duration;
	@Parsed
	String orderFillPercentage;
	@Parsed
	Order.TriggerCondition trigger;
	double triggerPrice;

	double fillPct;
	boolean isShort;
	boolean isBuy;

	private Order order;
	private final Trade trade;
	private final Trader trader;


	public OrderExecutionLine(Order order, Trade trade, Trader trader, Client client) {
		SymbolPriceDetails priceDetails = trader.priceDetails();
		SymbolPriceDetails refPriceDetails = trader.referencePriceDetails();

		this.order = order;
		this.trade = trade;
		this.trader = trader;

		clientId = client == null ? "N/A" : client.getId();
		referenceCurrency = trader.referenceCurrencySymbol();

		freeBalanceReferenceCurrency = Double.parseDouble(refPriceDetails.priceToString(trader.freeBalance()));
		holdings = refPriceDetails.priceToString(trader.holdings());

		double priceAmount = order == null || order.getPrice() == 0.0 ? trader.latestCandle().close : order.getPrice();

		if (order != null) {
			fundSymbol = trader.fundSymbol();
			assetSymbol = trader.assetSymbol();
			closeTime = trader.latestCandle().closeTimestamp();

			Balance balance = trader.balanceOf(fundSymbol);
			shortedQuantity = trader.shortedQuantity();

			SymbolPriceDetails amountDetails = fundSymbol.equals(referenceCurrency) ? refPriceDetails : priceDetails;

			freeBalance = balance.getFree();
			marginReserve = balance.getMarginReserve(trader.assetSymbol());
			valueTransacted = order.getTotalTraded();
			price = order.isBuy() ? order.getPrice() : priceAmount;
			averagePrice = order.getAveragePrice();

			orderId = order.getOrderId();
			quantity = order.getQuantity();
			status = order.getStatus();
			operation = order.sideDescription();
			isShort = order.isShort();
			isBuy = order.isBuy();
			orderType = order.getType();
			executedQuantity = order.getExecutedQuantity();
			fillPct = order.getFillPct();
			orderFillPercentage = order.getFormattedFillPct();

			if (orderType == Order.Type.MARKET && fillPct == 0.0) {
				orderAmount = order.getQuantity() * trader.lastClosingPrice();
			} else {
				orderAmount = order.getTotalOrderAmount();
			}
			this.trigger = order.getTriggerCondition();
			this.triggerPrice = order.getTriggerPrice();
		} else {
			operation = "END";
		}
		if (trade != null) {
			tradeId = trade.id();
			priceChangePct = trade.formattedPriceChangePct();
			if (trade.isFinalized()) {
				profitLoss = Double.parseDouble(priceDetails.priceToString(trade.actualProfitLoss()));
				profitLossPct = trade.formattedProfitLossPct();
				profitLossReferenceCurrency = Double.parseDouble(refPriceDetails.priceToString(trade.actualProfitLossInReferenceCurrency()));
			}
			minChangePct = trade.minChange();
			maxChangePct = trade.maxChange();
			minPrice = trade.minPrice();
			maxPrice = trade.maxPrice();
			ticks = trade.ticks();
			exitReason = trade.exitReason();

			if (order != null) {
				estimatedProfitLossPct = trade.formattedEstimateProfitLossPercentage(order);
				duration = TimeInterval.getFormattedDurationShort(order.getTimeElapsed(trade.latestCandle().closeTime));
			}
		}
	}

	public String printMinChange() {
		return isShort ? getFormattedMaxChangePct() : getFormattedMinChangePct();
	}

	public String printMaxChange() {
		return isShort ? getFormattedMinChangePct() : getFormattedMaxChangePct();
	}

	public String printMaxPriceAndChange() {
		return concat(getFormattedMaxPrice(), printMaxChange());
	}

	public String printMinPriceAndChange() {
		return concat(getFormattedMaxPrice(), printMinChange());
	}

	public String printLastPriceAndChange() {
		return concat(getFormattedPrice(), getFormattedPriceChangePct());
	}

	private String getFormattedPriceChangePct() {
		return priceChangePct;
	}

	public String printProfitLossAndChange() {
		return concat(getFormattedProfitLoss(), profitLossPct);
	}

	public String printReferenceCurrencyProfitLossAndChange() {
		return referenceCurrency + " " + concat(profitLossReferenceCurrency.toString(), profitLossPct);
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

	@Override
	public String toString() {
		return "{" +
				"tradeId=" + tradeId +
				", orderId='" + orderId + '\'' +
				", closeTime=" + closeTime +
				", clientId='" + clientId + '\'' +
				", assetSymbol='" + assetSymbol + '\'' +
				", operation='" + operation + '\'' +
				", exitReason='" + exitReason + '\'' +
				", ticks=" + ticks +
				", fundSymbol='" + fundSymbol + '\'' +
				", minPrice='" + getFormattedMinPrice() + '\'' +
				", minChangePct='" + getFormattedMinChangePct() + '\'' +
				", maxChangePct='" + getFormattedMaxChangePct() + '\'' +
				", maxPrice='" + getFormattedMaxPrice() + '\'' +
				", price='" + getFormattedPrice() + '\'' +
				", averagePrice='" + getFormattedAveragePrice() + '\'' +
				", priceChangePct='" + getFormattedPriceChangePct() + '\'' +
				", quantity='" + getFormattedQuantity() + '\'' +
				", referenceCurrency='" + referenceCurrency + '\'' +
				", status=" + status +
				", orderType=" + orderType +
				", orderAmount='" + getFormattedOrderAmount() + '\'' +
				", executedQuantity='" + getFormattedExecutedQuantity() + '\'' +
				", valueTransacted='" + getFormattedValueTransacted() + '\'' +
				", freeBalance='" + getFormattedFreeBalance() + '\'' +
				", shortedQuantity='" + getFormattedShortedQuantity() + '\'' +
				", marginReserve='" + getFormattedMarginReserve() + '\'' +
				", freeBalanceReferenceCurrency='" + getFormattedFreeBalanceReferenceCurrency() + '\'' +
				", estimatedProfitLossPct='" + estimatedProfitLossPct + '\'' +
				", profitLoss='" + getFormattedProfitLoss() + '\'' +
				", profitLossPct='" + profitLossPct + '\'' +
				", profitLossReferenceCurrency='" + profitLossReferenceCurrency.toString() + '\'' +
				", holdings='" + holdings + '\'' +
				", duration='" + duration + '\'' +
				", orderFillPercentage='" + orderFillPercentage + '\'' +
				", trigger=" + trigger +
				", triggerPrice='" + getFormattedTriggerPrice() + '\'' +
				", fillPct=" + fillPct +
				", isShort=" + isShort +
				", isBuy=" + isBuy +
				'}';
	}


	@Parsed(field = "executedQuantity")
	public String getFormattedExecutedQuantity(){
		SymbolPriceDetails priceDetails = trader.priceDetails();
		return order == null ? null : priceDetails.quantityToString(order.getExecutedQuantity());
	}

	@Parsed(field = "minPrice")
	public String getFormattedMinPrice(){
		SymbolPriceDetails priceDetails = trader.priceDetails();

		return trade == null ? null : priceDetails.priceToString(trade.minPrice());
	}

	@Parsed(field = "maxPrice")
	public String getFormattedMaxPrice(){
		SymbolPriceDetails priceDetails = trader.priceDetails();
		return trade == null ? null : priceDetails.priceToString(trade.maxPrice());
	}

	@Parsed(field = "quantity")
	public String getFormattedQuantity() {

		SymbolPriceDetails priceDetails = this.trader.priceDetails();
		return order == null ? null : priceDetails.quantityToString(order.getQuantity());
	}

	@Parsed(field = "averagePrice")
	public String getFormattedAveragePrice(){
		SymbolPriceDetails details = getAmountDetails();
		return order == null || details == null? null : details.priceToString(order.getAveragePrice());
	}

	@Parsed(field = "price")
	public String getFormattedPrice() {

		if (order == null){
			return null;
		}
		SymbolPriceDetails priceDetails = this.trader.priceDetails();
		SymbolPriceDetails refPriceDetails = trader.referencePriceDetails();

		SymbolPriceDetails amountDetails = fundSymbol.equals(referenceCurrency) ? refPriceDetails : priceDetails;
		double priceAmount = order.getPrice() == 0.0 ? trader.latestCandle().close : order.getPrice();

		return this.order.isBuy() ? amountDetails.priceToString(order.getPrice()) : priceDetails.priceToString(priceAmount);
	}

	@Parsed(field = "freeBalance")
	public String getFormattedFreeBalance() {
		if (order == null){
			return null;
		}
		SymbolPriceDetails amountDetails = getAmountDetails();
		return amountDetails != null ? amountDetails.priceToString(freeBalance) : null;
	}

	@Parsed(field = "freeBalanceReferenceCurrency")
	public String getFormattedFreeBalanceReferenceCurrency() {
		return freeBalanceReferenceCurrency.toString();
	}

	@Parsed(field = "shortedQuantity")
	public String getFormattedShortedQuantity() {

		SymbolPriceDetails priceDetails = this.trader.priceDetails();
		return trade == null || order == null ? null : priceDetails.quantityToString(trader.shortedQuantity());
	}

	@Parsed(field = "marginReserve")
	public String getFormattedMarginReserve() {
		if (order == null){
			return null;
		}
		SymbolPriceDetails amountDetails = getAmountDetails();
		Balance balance = trader.balanceOf(fundSymbol);
		return amountDetails != null ? amountDetails.priceToString(balance.getMarginReserve(trader.assetSymbol())) : null;
	}

	private SymbolPriceDetails getAmountDetails() {
		if (fundSymbol == null){
			return null;
		}
		SymbolPriceDetails priceDetails = this.trader.priceDetails();
		SymbolPriceDetails refPriceDetails = trader.referencePriceDetails();
		return fundSymbol.equals(referenceCurrency) ? refPriceDetails : priceDetails;
	}

	@Parsed(field = "orderAmount")
	public String getFormattedOrderAmount(){
		SymbolPriceDetails details = getAmountDetails();
		return order == null || details == null ? null : details.priceToString(orderAmount);
	}

	@Parsed(field = "valueTransacted")
	public String getFormattedValueTransacted() {
		if (order == null){
			return null;
		}
		SymbolPriceDetails amountDetails = getAmountDetails();
		return amountDetails != null ? amountDetails.priceToString(order.getTotalTraded()) : null;
	}

	@Parsed(field = "triggerPrice")
	public String getFormattedTriggerPrice(){
		SymbolPriceDetails priceDetails = this.trader.priceDetails();
		return priceDetails.priceToString(triggerPrice);

	}

	@Parsed(field = "minChangePct")
	public String getFormattedMinChangePct(){
		return trade == null ? null : trade.formattedMinChangePct();
	}

	@Parsed(field = "maxChangePct")
	public String getFormattedMaxChangePct(){
		return trade == null ? null : trade.formattedMaxChangePct();
	}

	public String getFormattedProfitLoss(){
		return profitLoss.toString();
	}
}
