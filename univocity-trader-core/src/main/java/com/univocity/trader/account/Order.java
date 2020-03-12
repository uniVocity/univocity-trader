package com.univocity.trader.account;

import com.univocity.trader.indicators.base.*;

import java.util.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Order.TriggerCondition.*;
import static com.univocity.trader.candles.Candle.*;

public interface Order extends Comparable<Order> {

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

	default String getFormattedTimeElapsed() {
		return TimeInterval.getFormattedDuration(getTimeElapsed());
	}

	default String getFormattedTimeElapsed(long latestClose) {
		return TimeInterval.getFormattedDuration(getTimeElapsed(latestClose));
	}

	String getOrderId();

	double getPrice();

	double getAveragePrice();

	double getQuantity();

	double getExecutedQuantity();

	double getFeesPaid();

	Order.Side getSide();

	Trade.Side getTradeSide();

	Type getType();

	TriggerCondition getTriggerCondition();

	double getTriggerPrice();

	boolean isActive();

	long getTime();

	Status getStatus();

	void cancel();

	default Order getParent() {
		return null;
	}

	default List<Order> getAttachments() {
		return Collections.emptyList();
	}

	default boolean isCancelled() {
		return getStatus() == Status.CANCELLED;
	}

	default double getRemainingQuantity() {
		return getQuantity() - getExecutedQuantity();
	}

	default double getTotalTraded() {
		return getExecutedQuantity() * getAveragePrice();
	}

	default double getTotalOrderAmount() {
		return getQuantity() * getPrice();
	}

	default boolean isFinalized() {
		return getStatus() == FILLED || getStatus() == Status.CANCELLED;
	}

	default String getParentOrderId() {
		return getParent() == null ? "" : getParent().getOrderId();
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

		description.append(getStatus()).append(' ');

		if (getTriggerCondition() != NONE) {
			description
					.append(getTriggerCondition())
					.append("[")
					.append(roundStr(getTriggerPrice()))
					.append(']').append(' ');
		}

		description.append(getType()).append(' ');

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

		if (getStatus() == Status.PARTIALLY_FILLED || isFinalized() && getExecutedQuantity() > 0) {

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
		if (getQuantity() == 0.0) {
			return 0.0;
		}
		return getExecutedQuantity() / getQuantity() * 100.0;
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

	@Override
	default int compareTo(Order o) {
		return this.getOrderId().compareTo(o.getOrderId());
	}

	enum Side {
		BUY,
		SELL
	}

	enum Type {
		LIMIT,
		MARKET
	}

	enum TriggerCondition {
		NONE(""),
		STOP_LOSS("SL"),
		STOP_GAIN("SG");

		public final String shortName;

		TriggerCondition(String shortName) {
			this.shortName = shortName;
		}
	}

	enum Status {
		NEW,
		PARTIALLY_FILLED,
		FILLED,
		CANCELLED
	}
}
