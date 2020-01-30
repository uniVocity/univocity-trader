package com.univocity.trader.account;

import org.apache.commons.lang3.*;

import java.math.*;
import java.util.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;

public class OrderRequest {

	private boolean cancelled = false;
	private final String assetsSymbol;
	private final String fundsSymbol;
	private final Order.Side side;
	private final Trade.Side tradeSide;
	private final long time;
	private final Order resubmittedFrom;

	private BigDecimal price = BigDecimal.ZERO;
	private BigDecimal quantity = BigDecimal.ZERO;
	private Order.Type type = Order.Type.LIMIT;

	private List<OrderRequest> attachments = new ArrayList<>();

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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = round(price);
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = round(quantity);
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

	public BigDecimal getTotalOrderAmount() {
		return round(price.multiply(quantity));
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "OrderPreparation{" +
				"symbol='" + getSymbol() + '\'' +
				", side=" + side +
				", price=" + price +
				", quantity=" + quantity +
				", type=" + type +
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

	public final List<OrderRequest> getRequestAttachments() {
		return attachments == null ? null : Collections.unmodifiableList(attachments);
	}

	public OrderRequest attach(Order.Type type, double change) {
		if (attachments == null) {
			throw new IllegalArgumentException("Can only attach orders to the parent order");
		}
		for (OrderRequest attachment : attachments) {
			if (attachment.side == side && type == attachment.type) {
				return attachment;
			}
		}

		OrderRequest attachment = new OrderRequest(assetsSymbol, fundsSymbol, side == BUY ? SELL : BUY, this.tradeSide, this.time, null);
		attachment.attachments = null;

		this.attachments.add(attachment);
		attachment.setQuantity(this.quantity);
		attachment.setPrice(this.price.multiply(BigDecimal.valueOf(1.0 + (change / 100.0))));
		return attachment;
	}
}
