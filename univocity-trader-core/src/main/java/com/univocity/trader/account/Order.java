package com.univocity.trader.account;

import com.univocity.trader.indicators.base.*;

import java.math.*;

import static com.univocity.trader.account.Balance.*;

public interface Order {

	String getAssetsSymbol();

	String getFundsSymbol();

	default String getSymbol() {
		return getAssetsSymbol() + getFundsSymbol();
	}

	default long getTimeElapsed() {
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

	default boolean isCancelled() {
		return getStatus() == Status.CANCELLED;
	}

	default BigDecimal getRemainingQuantity(){
		return round(getQuantity().subtract(getExecutedQuantity()));
	}

	default BigDecimal getTotalTraded() {
		return round(getExecutedQuantity().multiply(getPrice()));
	}

	default BigDecimal getTotalOrderAmount() {
		return round(getQuantity().multiply(getPrice()));
	}

	default boolean isFinalized() {
		return getStatus() == Status.FILLED || getStatus() == Status.CANCELLED;
	}

	default String print() {
		StringBuilder description = new StringBuilder();

		description
				.append(getStatus()).append(' ')
				.append(getType()).append(' ')
				.append(getSide()).append(' ')
				.append(roundStr(getQuantity())).append(' ')
				.append(getAssetsSymbol());

		if (getType() == Type.LIMIT) {
			description
					.append(" @ ")
					.append(roundStr(getPrice())).append(' ');

			description
					.append(" (Total: ")
					.append(roundStr(getTotalOrderAmount()))
					.append(')');

			description.append(' ').append(getFundsSymbol());

		} else if (getType() == Type.MARKET) {
			description.append(getFundsSymbol());
		}

		if (getType() == Type.MARKET) {
			description
					.append(" @ ")
					.append(roundStr(getPrice())).append(' ')
					.append(getFundsSymbol());
		}

		if (getStatus() == Status.PARTIALLY_FILLED || isFinalized() && getExecutedQuantity().compareTo(BigDecimal.ZERO) > 0) {

			description
					.append(" - filled: ")
					.append(roundStr(getExecutedQuantity()));

			description
					.append(", worth ~")
					.append(roundStr(getTotalTraded())).append(' ')
					.append(getFundsSymbol());
		}

		description
				.append(". Open for ")
				.append(TimeInterval.getFormattedDuration(getTimeElapsed()));

		description.append('.');
		return description.toString();
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
