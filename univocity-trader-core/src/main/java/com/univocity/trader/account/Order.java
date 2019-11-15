package com.univocity.trader.account;

import java.math.*;

public interface Order {

	String getAssetsSymbol();

	String getFundsSymbol();

	default String getSymbol() {
		return getAssetsSymbol() + getFundsSymbol();
	}

	default long getTimeElapsed(){
		return System.currentTimeMillis() - getTime();
	}

	String getOrderId();

	BigDecimal getPrice();

	BigDecimal getQuantity();

	BigDecimal getExecutedQuantity();

	Side getSide();

	Type getType();

	Long getTime();

	Status getStatus();

	void cancel();

	default BigDecimal getTotalSpent() {
		return getExecutedQuantity().multiply(getPrice());
	}

	default BigDecimal getTotalOrderAmount() {
		return getQuantity().multiply(getPrice());
	}

	enum Side {
		BUY,
		SELL
	}

	enum Type {
		LIMIT,
		MARKET
	}

	enum Status {
		NEW,
		PARTIALLY_FILLED,
		FILLED,
		CANCELLED
	}
}
