package com.univocity.trader.simulation;

import com.univocity.trader.account.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.orderfill.*;

public class MockClientAccount extends SimulatedClientAccount {

	public MockClientAccount(AccountConfiguration<?> accountConfiguration) {
		super(accountConfiguration, new ImmediateFillEmulator(), SimpleTradingFees.percentage(0.0));
	}

	@Override
	public Order updateOrderStatus(Order order) {
		DefaultOrder out = new DefaultOrder(((DefaultOrder) order).getInternalId(), order.getAssetsSymbol(), order.getFundsSymbol(), order.getSide(), Trade.Side.LONG, order.getTime());
		out.setStatus(order.getStatus());

		if (!order.isFinalized()) {
			out.setExecutedQuantity(Math.min(order.getExecutedQuantity() + 30, order.getQuantity()));
			if (out.getExecutedQuantity() > 0) {
				if (out.getExecutedQuantity() < order.getQuantity()) {
					out.setStatus(Order.Status.PARTIALLY_FILLED);
				} else {
					out.setStatus(Order.Status.FILLED);
				}
			}
		} else {
			out.setExecutedQuantity(order.getExecutedQuantity());
		}

		out.setAveragePrice(order.getPrice());
		out.setPrice(order.getPrice());
		out.setOrderId(String.valueOf(order.getOrderId()));
		out.setType(order.getType());
		out.setQuantity(order.getQuantity());
		out.setTrade(order.getTrade());
		return out;
	}

	@Override
	public boolean isSimulated() {
		return false;
	}
}
