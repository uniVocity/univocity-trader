package com.univocity.trader.account;

import org.slf4j.*;

import java.util.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.account.TradingManager.*;

public final class OrderTracker {

	private static final Logger log = LoggerFactory.getLogger(OrderTracker.class);

	final OrderSet pendingOrders = new OrderSet();
	final OrderSet orderUpdates = new OrderSet();

	private final TradingManager tradingManager;
	private final AccountManager account;
	private final OrderManager orderManager;
	private final Trader trader;

	OrderTracker(TradingManager tradingManager) {
		this.tradingManager = tradingManager;
		this.account = tradingManager.getAccount();
		this.orderManager = tradingManager.orderManager;
		this.trader = tradingManager.trader;
	}


	public void waitForFill(Order order) {
		pendingOrders.addOrReplace(order);
		if (account.isSimulated()) {
			return;
		}
		new Thread(() -> {
			Thread.currentThread().setName("Order " + order.getOrderId() + " monitor: " + order.getSide() + " " + order.getSymbol());
			Order o = order;
			while (true) {
				try {
					try {
						Thread.sleep(orderManager.getOrderUpdateFrequency().ms);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					if (!o.isFinalized()) {
						o = tradingManager.getAccount().updateOrderStatus(o);
					} else {
						o = null;
					}
					if (o != null) {
						if (order.getTrade() == null) {
							throw new IllegalStateException("Trade associated with order " + order + " can't be null");
						}
						synchronized (orderUpdates) {
							orderUpdates.addOrReplace(o);
						}
					}
					if (o == null || o.isFinalized()) {
						return;
					}
				} catch (Exception e) {
					log.error("Error tracking state of order " + o, e);
					return;
				}
			}
		}).start();
	}

	public boolean waitingForFill(String assetSymbol, Order.Side side, Trade.Side tradeSide) {
		for (int i = pendingOrders.i - 1; i >= 0; i--) {
			Order order = pendingOrders.elements[i];

			if (order.isFinalized() || order.getTradeSide() != tradeSide) {
				continue;
			}

			if (order.getSide() == side && order.getAssetsSymbol().equals(assetSymbol)) {
				return true;
			}
			// If we want to know if there is an open order to buy BTC,
			// and the symbol is "ADABTC", we need to invert the side as
			// we are selling ADA to buy BTC.
			if (side == BUY && order.isSell() && order.getFundsSymbol().equals(assetSymbol)) {
				return true;

			} else if (side == SELL && order.isBuy() && order.getFundsSymbol().equals(assetSymbol)) {
				return true;
			}

		}
		return false;
	}

	void orderFinalized(Order order) {
		try {
			account.executeUpdateBalances();
		} finally {
			notifyFinalized(order, trader);
			List<Order> attachments;
			if (order.getParent() != null) {
				attachments = order.getParent().getAttachments();
			} else {
				attachments = order.getAttachments();
			}
			if (attachments != null && order.isCancelled() && order.getExecutedQuantity() == 0.0) {
				for (Order attached : attachments) {
					if (attached != order) {
						attached.cancel();
						notifyFinalized(attached, trader);
					}
				}
			}
		}
	}

	private void notifyFinalized(Order order, Trader trader) {
		try {
			orderManager.finalized(order, trader);
		} finally {
			tradingManager.notifyOrderFinalized(order);
		}
	}

	void initiateOrderMonitoring(Order order) {
		if (order != null) {
			if (order.getTrade() == null) {
				throw new IllegalStateException("Order " + order + " does not have a valid trade associated with it.");
			}
			switch (order.getStatus()) {
				case NEW:
				case PARTIALLY_FILLED:
					logOrderStatus("Tracking pending order. ", order);
					waitForFill(order);
					break;
				case FILLED:
					logOrderStatus("Completed order. ", order);
					orderFinalized(order);
					break;
				case CANCELLED:
					logOrderStatus("Could not create order. ", order);
					orderFinalized(order);
					break;
			}
		}
	}

	public void removePendingOrder(Order order) {
		pendingOrders.remove(order);
		synchronized (orderUpdates) {
			orderUpdates.remove(order);
		}
	}


	public void updateOpenOrders() {
		synchronized (orderUpdates) {
			for (int i = orderUpdates.i - 1; i >= 0; i--) {
				Order order = orderUpdates.elements[i];
				updateOrder(order);
			}
		}
	}

	Order cancelOrder(Order order) {
		try {
			order.cancel();
			account.cancel(order);
		} catch (Exception e) {
			log.error("Failed to execute cancellation of order '" + order + "' on exchange", e);
		} finally {
			removePendingOrder(order);
			order = account.updateOrderStatus(order);
			orderFinalized(order);
			logOrderStatus("Cancellation via order manager: ", order);
		}
		return order;
	}

	public Order updateOrder(Order order) {
		if (order.getTrade() != null) {
			if (!order.getTrade().orderUpdated(order)) {
				removePendingOrder(order);
				return order;
			}
		}

		Order update = order;
		if (!account.isSimulated()) {
			if (pendingOrders.contains(order)) {
				order = pendingOrders.get(order);
			}
			synchronized (orderUpdates) {
				update = orderUpdates.get(order);
			}
			if (update == null) {
				log.warn("Lost track of order {}. Trying to cancel it.", order);
				update = cancelOrder(order);
			}
		}

		if (update.isFinalized()) {
			logOrderStatus("Order finalized. ", update);
			removePendingOrder(update);
			orderFinalized(update);
			return update;
		} else { // update order status
			pendingOrders.addOrReplace(update);
		}

		if (update.getExecutedQuantity() != order.getExecutedQuantity() || (account.isSimulated() && update instanceof DefaultOrder && ((DefaultOrder) update).hasPartialFillDetails())) {
			logOrderStatus("Order updated. ", update);
			account.executeUpdateBalances();
			orderManager.updated(update, trader, tradingManager::resubmit);
		} else {
			logOrderStatus("Unchanged ", update);
			orderManager.unchanged(update, trader, tradingManager::resubmit);
		}

		//order manager could have cancelled the order
		if (update.getStatus() == CANCELLED && pendingOrders.contains(update)) {
			cancelOrder(update);
		}
		return update;
	}

	public void cancelStaleOrdersFor(Trade.Side side, Trader trader) {
		account.forEachTradingManager(tradingManager -> {
			if (!tradingManager.symbol.equals(this.tradingManager.symbol)) {
				for (int i = pendingOrders.i - 1; i >= 0; i--) {
					Order order = pendingOrders.elements[i];
					if (order.isFinalized()) {
						continue;
					}
					if (tradingManager.orderManager.cancelToReleaseFundsFor(order, tradingManager.trader, trader)) {
						if (order.getStatus() == CANCELLED) {
							cancelOrder(order);
							return;
						}
					}
				}
			}
		});
	}

	public void cancelAllOrders() {
		synchronized (orderUpdates) {
			for (int i = pendingOrders.i - 1; i >= 0; i--) {
				Order order = pendingOrders.elements[i];
				order.cancel();
				orderUpdates.addOrReplace(order);
			}
		}
	}

	public void clear() {
		pendingOrders.clear();
		synchronized (orderUpdates) {
			orderUpdates.clear();
		}
	}
}
