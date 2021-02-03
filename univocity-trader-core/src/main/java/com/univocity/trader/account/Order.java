package com.univocity.trader.account;

import com.univocity.trader.indicators.base.*;

import java.util.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.Order.TriggerCondition.*;
import static com.univocity.trader.candles.Candle.*;

public class Order extends OrderRequest implements Comparable<Order> {

	private final long id;
	private String orderId;
	private double executedQuantity = 0.0;
	private Status status;
	private double feesPaid = 0.0;
	private double averagePrice = 0.0;
	private List<Order> attachments;
	private Order parent;
	private double partialFillPrice = 0.0;
	private double partialFillQuantity = 0.0;
	private Trade trade;
	public boolean processed = false;

	public Order(long id, String assetSymbol, String fundSymbol, Side side, Trade.Side tradeSide, long time) {
		super(assetSymbol, fundSymbol, side, tradeSide, time, null);
		this.id = id;
		this.orderId = String.valueOf(id);
	}

	public Order(long id, OrderRequest request) {
		this(id, request.getAssetsSymbol(), request.getFundsSymbol(), request.getSide(), request.getTradeSide(), request.getTime());
	}

	public void setParent(Order parent) {
		this.parent = parent;
		if (parent.attachments == null) {
			parent.attachments = new ArrayList<>();
		}
		parent.attachments.add(this);
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public double getExecutedQuantity() {
		return executedQuantity;
	}

	public double getTotalOrderAmountAtAveragePrice() {
		if (averagePrice == 0) {
			return getPrice() * getQuantity();
		}
		return averagePrice * getQuantity();
	}

	public void setExecutedQuantity(double executedQuantity) {
		this.executedQuantity = executedQuantity;
	}

	@Override
	public void setPrice(double price) {
		super.setPrice(price);
	}

	@Override
	public void setQuantity(double quantity) {
		super.setQuantity(quantity);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void cancel() {
		if (this.status != Status.FILLED) {
			this.status = Status.CANCELLED;
		}
	}

	public boolean isCancelled() {
		return this.status == Status.CANCELLED;
	}

	public double getFeesPaid() {
		return feesPaid;
	}

	public void setFeesPaid(double feesPaid) {
		this.feesPaid = feesPaid;
	}

	@Override
	public String toString() {
		return print(0);
	}

	public void setAveragePrice(double averagePrice) {
		this.averagePrice = averagePrice;
	}

	public final double getAveragePrice() {
		return averagePrice;
	}

	public final List<Order> getAttachments() {
		return attachments == null ? null : Collections.unmodifiableList(attachments);
	}

	public final Order getParent() {
		return parent;
	}

	public final String getParentOrderId() {
		return parent == null ? "" : parent.getOrderId();
	}

	public double getQuantity() { //TODO: check this implementation in live trading.
		double out = super.getQuantity();
		if (parent != null && parent.isFinalized()) {
			double p = parent.getExecutedQuantity();
			if (out > p || p == 0) {
				return p;
			}
		}
		return out;
	}

	public boolean hasPartialFillDetails() {
		return partialFillQuantity != 0.0 && partialFillQuantity > 0;
	}

	public void clearPartialFillDetails() {
		partialFillQuantity = 0.0;
		partialFillPrice = 0.0;
	}

	public double getPartialFillTotalPrice() {
		return partialFillQuantity * partialFillPrice;
	}

	public double getPartialFillPrice() {
		return partialFillPrice;
	}

	public double getPartialFillQuantity() {
		return partialFillQuantity;
	}

	public void setPartialFillDetails(double filledQuantity, double fillPrice) {
		this.partialFillPrice = fillPrice;
		this.partialFillQuantity = filledQuantity;
	}

	public long getInternalId() {
		return id;
	}

	@Override
	public int compareTo(Order o) {
		return Long.compare(this.id, o.id);
	}

	public Trade getTrade() {
		return trade;
	}

	public void setTrade(Trade trade) {
		this.trade = trade;
	}

	public long getTimeElapsed() {
		return System.currentTimeMillis() - getTime();
	}

	public long getTimeElapsed(long latestClose) {
		return latestClose - getTime();
	}

	public String getFormattedTimeElapsed() {
		return TimeInterval.getFormattedDuration(getTimeElapsed());
	}

	public String getFormattedTimeElapsed(long latestClose) {
		return TimeInterval.getFormattedDuration(getTimeElapsed(latestClose));
	}

	public double getRemainingQuantity() {
		return getQuantity() - getExecutedQuantity();
	}

	public double getTotalTraded() {
		return getExecutedQuantity() * getAveragePrice();
	}

	public boolean isFinalized() {
		return getStatus() == FILLED || getStatus() == Status.CANCELLED;
	}

	public String print(long latestClose) {
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

	public String getFormattedFillPct() {
		String out = CHANGE_FORMAT.get().format(getFillPct() / 100.0);
		//adjust display of mostly filled order so it's less confusing.
		if (getStatus() != FILLED && out.equals("100.00%")) {
			return "99.99%";
		}
		return out;
	}

	public double getFillPct() {
		if (getQuantity() == 0.0) {
			return 0.0;
		}
		return getExecutedQuantity() / getQuantity() * 100.0;
	}

	public String sideDescription() {
		if (isShort()) {
			if (isSell()) {
				return "SHORT";
			} else if (isBuy()) {
				return "COVER";
			}
		}
		return getSide().toString();
	}

	public enum Side {
		BUY,
		SELL
	}

	public enum Type {
		LIMIT,
		MARKET
	}

	public enum TriggerCondition {
		NONE(""),
		STOP_LOSS("SL"),
		STOP_GAIN("SG");

		public final String shortName;

		TriggerCondition(String shortName) {
			this.shortName = shortName;
		}
	}

	public enum Status {
		NEW,
		PARTIALLY_FILLED,
		FILLED,
		CANCELLED
	}
}
