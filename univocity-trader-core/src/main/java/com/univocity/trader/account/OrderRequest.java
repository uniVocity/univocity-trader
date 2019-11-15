package com.univocity.trader.account;

import org.apache.commons.lang3.*;

import java.math.*;

public class OrderRequest {

	private final String assetsSymbol;
	private final String fundsSymbol;
	private final Order.Side side;

	private BigDecimal price = BigDecimal.ZERO;
	private BigDecimal quantity = BigDecimal.ZERO;
	private Order.Type type = Order.Type.LIMIT;

	public OrderRequest(String assetsSymbol, String fundsSymbol, Order.Side side) {
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
		this.price = price;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public Order.Side getSide() {
		return side;
	}

	public Order.Type getType() {
		return type;
	}

	public void setType(Order.Type type) {
		this.type = type;
	}

	public BigDecimal getTotalOrderAmount() {
		return price.multiply(quantity);
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
}
