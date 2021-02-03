package com.univocity.trader.account;

import org.apache.commons.lang3.*;

import java.util.*;
import java.util.function.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.TriggerCondition.*;
import static com.univocity.trader.account.Order.Type.*;

public class OrderRequest {

	private boolean cancelled = false;
	private final String assetsSymbol;
	private final String fundsSymbol;
	private final Order.Side side;
	private final Trade.Side tradeSide;
	private long time;
	private final Order resubmittedFrom;

	private double triggerPrice;
	private Order.TriggerCondition triggerCondition = NONE;

	private double price = 0.0;
	private double quantity = 0.0;
	private Order.Type type = LIMIT;
	private boolean active = true;

	private List<OrderRequest> attachedRequests = null;

	public OrderRequest(String assetsSymbol, String fundsSymbol, Order.Side side, Trade.Side tradeSide, long time, Order resubmittedFrom) {
		this.resubmittedFrom = resubmittedFrom;
		this.time = time;
		if (StringUtils.isBlank(assetsSymbol)) {
			throw new IllegalArgumentException("Assets symbol cannot be null/blank");
		}
		if (StringUtils.isBlank(fundsSymbol)) {
			throw new IllegalArgumentException("Funds symbol cannot be null/blank");
		}
		if (side == null) {
			throw new IllegalArgumentException("Order side cannot be null");
		}
		this.assetsSymbol = assetsSymbol;
		this.fundsSymbol = fundsSymbol;
		this.side = side;
		this.tradeSide = tradeSide;
	}

	public String getAssetsSymbol() {
		return assetsSymbol;
	}

	public String getFundsSymbol() {
		return fundsSymbol;
	}

	public String getSymbol() {
		return assetsSymbol + fundsSymbol;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public Order.Side getSide() {
		return side;
	}

	public Trade.Side getTradeSide() {
		return tradeSide;
	}

	public Order.Type getType() {
		return type;
	}

	public void setType(Order.Type type) {
		this.type = type;
	}

	public double getTotalOrderAmount() {
		return price * quantity;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "OrderPreparation{" +
				"symbol='" + getSymbol() + '\'' +
				(triggerCondition == NONE ? "" : ", {" + triggerCondition + "@" + triggerPrice + "}") +
				", type=" + type +
				", tradeSide=" + tradeSide +
				", side=" + side +
				", price=" + price +
				", quantity=" + quantity +
				'}';
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void cancel() {
		cancelled = true;
	}

	public final boolean isResubmission() {
		return resubmittedFrom != null;
	}

	public final Order getOriginalOrder() {
		return resubmittedFrom;
	}

	public boolean isLongBuy() {
		return isLong() && isBuy();
	}

	public boolean isLongSell() {
		return isLong() && isSell();
	}

	public boolean isShortSell() {
		return isShort() && isSell();
	}

	public boolean isShortCover() {
		return isShort() && isBuy();
	}

	public final boolean isShort() {
		return tradeSide == Trade.Side.SHORT;
	}

	public final boolean isLong() {
		return tradeSide == Trade.Side.LONG;
	}

	public final boolean isBuy() {
		return side == Order.Side.BUY;
	}

	public final boolean isSell() {
		return side == SELL;
	}

	public boolean isMarket() {
		return getType() == MARKET;
	}

	public boolean isLimit() {
		return getType() == LIMIT;
	}

	public void updateTime(long time) {
		this.time = time;
	}

	public final List<OrderRequest> attachedOrderRequests() {
		return attachedRequests == null ? null : Collections.unmodifiableList(attachedRequests);
	}

	public final Order.TriggerCondition getTriggerCondition() {
		return this.triggerCondition;
	}

	public final double getTriggerPrice() {
		return this.triggerPrice;
	}

	public final void setTriggerCondition(Order.TriggerCondition triggerCondition, Double triggerPrice) {
		this.triggerCondition = triggerCondition;
		this.triggerPrice = triggerPrice;
		this.active = !(triggerCondition != NONE && triggerPrice != 0.0);
		if (this.price == 0.0 && triggerPrice != 0.0) {
			this.price = triggerPrice;
		}
	}

	public final void activate() {
		active = true;
	}

	public final boolean isActive() {
		return active && !isCancelled();
	}

	public OrderRequest attachToPercentageChange(Order.Type type, double percentageChange) {
		return attach(type, () -> this.price * (1.0 + (percentageChange / 100.0)));
	}

	public OrderRequest attachToPriceChange(Order.Type type, double priceChange) {
		return attach(type, ()->this.price + priceChange);
	}

	protected void setAttachedOrderRequests(List<OrderRequest> attachedRequests) {
		this.attachedRequests = attachedRequests == null ? null : new ArrayList<>(attachedRequests);
	}

	private OrderRequest attach(Order.Type type, DoubleSupplier priceSupplier) {
		if (attachedRequests == null) {
			attachedRequests = new ArrayList<>();
		}

		OrderRequest attachment = new OrderRequest(assetsSymbol, fundsSymbol, side == BUY ? SELL : BUY, this.tradeSide, this.time, null);
		attachment.attachedRequests = null;

		this.attachedRequests.add(attachment);
		attachment.setQuantity(this.quantity);

		double price = priceSupplier.getAsDouble();
		double diff = price - this.price;

		attachment.setPrice(price);
		attachment.setType(type);

		if (diff < 0.0) {
			attachment.setTriggerCondition(STOP_LOSS, attachment.getPrice());
		}

		if (diff >= 0.0) {
			attachment.setTriggerCondition(STOP_GAIN, attachment.getPrice());
		}

		return attachment;
	}
}
