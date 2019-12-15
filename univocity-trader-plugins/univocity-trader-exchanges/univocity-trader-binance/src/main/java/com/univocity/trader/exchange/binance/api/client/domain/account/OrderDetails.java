package com.univocity.trader.exchange.binance.api.client.domain.account;

import com.univocity.trader.exchange.binance.api.client.domain.*;

public interface OrderDetails {
	String getSymbol();

	Long getOrderId();

	String getClientOrderId();

	String getPrice();

	String getOrigQty();

	String getExecutedQty();

	OrderStatus getStatus();

	OrderType getType();

	OrderSide getSide();

	Long getTime();

	String getCummulativeQuoteQty();
}
