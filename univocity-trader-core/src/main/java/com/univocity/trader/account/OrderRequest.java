package com.univocity.trader.account;

import org.apache.commons.lang3.*;

import java.math.*;

import static com.univocity.trader.account.Balance.*;

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
		return side == Order.Side.SELL;
	}
}
