package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.orderfill.*;

import java.util.concurrent.*;

public class MockClientAccount implements ClientAccount {

	private final SimulatedClientAccount account;

	private OrderFillEmulator mockOrderFill = (order, candle) -> {
		if (!order.isFinalized()) {
			double increment = order.getQuantity() * 0.1;
			double fillQuantity = order.getExecutedQuantity() + increment;
			if (fillQuantity > order.getQuantity()) {
				fillQuantity = increment - (fillQuantity - order.getQuantity());
			} else {
				fillQuantity = increment;
			}

			order.setPartialFillDetails(fillQuantity, order.getPrice());
			order.setExecutedQuantity(order.getExecutedQuantity() + fillQuantity);

			if (order.getExecutedQuantity() > 0) {
				if (order.getExecutedQuantity() < order.getQuantity()) {
					order.setStatus(Order.Status.PARTIALLY_FILLED);
				} else {
					order.setStatus(Order.Status.FILLED);
				}
			}
		} else {
			order.setExecutedQuantity(order.getExecutedQuantity());
		}
	};

	public MockClientAccount(AccountConfiguration<?> accountConfiguration) {
		account = new SimulatedClientAccount(accountConfiguration, mockOrderFill, SimpleTradingFees.percentage(0.0), () -> null);
		account.accountManager.setAmount("USDT", 100);
		SimulatedExchange exchange = new SimulatedExchange(account.accountManager);
		accountConfiguration.getAllSymbolPairs().keySet().forEach(s -> account.accountManager.createTradingManager(s, exchange, null, Parameters.NULL));
	}

	@Override
	public Order updateOrderStatus(Order order) {
		synchronized (account) {
			return copy(account.updateOrderStatus(order));
		}
	}

	@Override
	public boolean isSimulated() {
		return false;
	}

	private Order copy(Order order) {
		if (order == null) {
			return null;
		}
		Order out = new Order(order.getInternalId(), order.getAssetsSymbol(), order.getFundsSymbol(), order.getSide(), Trade.Side.LONG, order.getTime());
		out.setStatus(order.getStatus());
		out.setExecutedQuantity(order.getExecutedQuantity());
		out.setAveragePrice(order.getPrice());
		out.setPrice(order.getPrice());
		out.setOrderId(String.valueOf(order.getOrderId()));
		out.setType(order.getType());
		out.setQuantity(order.getQuantity());
		out.setTrade(order.getTrade());
		return out;
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		synchronized (account) {
			return account.executeOrder(orderDetails);
		}
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances(boolean force) {
		synchronized (account) {
			account.accountManager.forEachTradingManager(TradingManager::updateOpenOrders);

			var tmp = account.updateBalances(force);
			var out = new ConcurrentHashMap<String, Balance>();
			tmp.forEach((k, v) -> out.put(k, v.clone()));

			return out;
		}
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	@Override
	public void cancel(Order order) {
		synchronized (account) {
			account.cancel(order);
		}
	}
}
