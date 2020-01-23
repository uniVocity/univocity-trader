package com.univocity.trader.account;

import com.univocity.trader.indicators.base.*;

import java.math.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.candles.Candle.*;

public interface Order {

	String getAssetsSymbol();

	String getFundsSymbol();

	default String getSymbol() {
		return getAssetsSymbol() + getFundsSymbol();
	}

	default long getTimeElapsed() {
		return System.currentTimeMillis() - getTime();
	}

	default long getTimeElapsed(long latestClose) {
		return latestClose - getTime();
	}

	String getOrderId();

	BigDecimal getPrice();

	BigDecimal getQuantity();

	BigDecimal getExecutedQuantity();

	BigDecimal getFeesPaid();

	Order.Side getSide();

	Trade.Side getTradeSide();

	Type getType();

	long getTime();

	Status getStatus();

	void cancel();

	default boolean isCancelled() {
		return getStatus() == Status.CANCELLED;
	}

	default BigDecimal getRemainingQuantity() {
		return round(getQuantity().subtract(getExecutedQuantity()));
	}

	default BigDecimal getTotalTraded() {
		return round(getExecutedQuantity().multiply(getPrice()));
	}

	default BigDecimal getTotalOrderAmount() {
		return round(getQuantity().multiply(getPrice()));
	}

	default boolean isFinalized() {
		return getStatus() == FILLED || getStatus() == Status.CANCELLED;
	}

	default boolean isBuy() {
		return getSide() == Side.BUY;
	}

	default boolean isLongBuy() {
		return isLong() && isBuy();
	}

	default boolean isLongSell() {
		return isLong() && isSell();
	}

	default boolean isShortSell() {
		return isShort() && isSell();
	}

	default boolean isShortCover() {
		return isShort() && isBuy();
	}

	default boolean isSell() {
		return getSide() == Side.SELL;
	}

	default boolean isMarket() {
		return getType() == Type.MARKET;
	}

	default boolean isLimit() {
		return getType() == Type.LIMIT;
	}

	default boolean isShort() {
		return getTradeSide() == Trade.Side.SHORT;
	}

	default boolean isLong() {
		return getTradeSide() == Trade.Side.LONG;
	}

	default String print(long latestClose) {
		StringBuilder description = new StringBuilder();

		description
				.append(getStatus()).append(' ')
				.append(getType()).append(' ');


		if (isShort()) {
			description.append(getTradeSide()).append(' ');
		}

		description
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

		if (latestClose > 0) {
			description
					.append(". Open for ")
					.append(TimeInterval.getFormattedDuration(getTimeElapsed(latestClose)));
		}

		description.append('.');
		return description.toString();
	}

	default String getFormattedFillPct() {
		String out = CHANGE_FORMAT.get().format(getFillPct() / 100.0);
		//adjust display of mostly filled order so it's less confusing.
		if (getStatus() != FILLED && out.equals("100.00%")) {
			return "99.99%";
		}
		return out;
	}

	default double getFillPct() {
		return getExecutedQuantity().divide(getQuantity(), RoundingMode.FLOOR).doubleValue() * 100.0;
	}

	default String sideDescription() {
		if (isShort()) {
			if (isSell()) {
				return "SHORT";
			} else if (isBuy()) {
				return "COVER";
			}
		}
		return getSide().toString();
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
