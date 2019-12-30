package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.orderfill.*;

import java.math.*;
import java.util.*;

import static com.univocity.trader.account.Balance.*;
import static com.univocity.trader.account.Order.Side.*;

public class SimulatedClientAccount implements ClientAccount {

	private Map<String, Set<PendingOrder>> orders = new HashMap<>();
	private TradingFees tradingFees;
	private final AccountManager account;
	private OrderFillEmulator orderFillEmulator;

	private static class PendingOrder {
		final Order order;
		final BigDecimal lockedAmount;

		public PendingOrder(Order order, BigDecimal lockedAmount) {
			this.order = order;
			this.lockedAmount = round(lockedAmount);
		}
	}

	public SimulatedClientAccount(AccountConfiguration<?> accountConfiguration, Simulation simulation) {
		this.account = new AccountManager(this, accountConfiguration, simulation);
		this.orderFillEmulator = simulation.orderFillEmulator();
	}

	public final TradingFees getTradingFees() {
		if (this.tradingFees == null) {
			this.tradingFees = account.getTradingFees();
			if (this.tradingFees == null) {
				throw new IllegalArgumentException("Trading fees cannot be null");
			}
		}
		return this.tradingFees;
	}

	@Override
	public synchronized Order executeOrder(OrderRequest orderDetails) {
		String fundsSymbol = orderDetails.getFundsSymbol();
		String assetsSymbol = orderDetails.getAssetsSymbol();
		Order.Type orderType = orderDetails.getType();
		double unitPrice = orderDetails.getPrice().doubleValue();
		final double orderAmount = orderDetails.getTotalOrderAmount().doubleValue();
		double availableFunds = account.getAmount(fundsSymbol);
		double availableAssets = account.getAmount(assetsSymbol);
		double quantity = orderDetails.getQuantity().doubleValue();
		double fees = orderAmount - getTradingFees().takeFee(orderAmount, orderType, orderDetails.getSide());

		BigDecimal locked = null;

		Order order = null;
		if (orderDetails.getSide() == BUY && availableFunds - fees >= orderAmount - 0.000000001) {
			locked = orderDetails.getTotalOrderAmount();
			account.lockAmount(fundsSymbol, locked);
			order = createOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, BUY, orderType, orderDetails.getTime());

		} else if (orderDetails.getSide() == SELL && availableAssets >= quantity) {
			locked = orderDetails.getQuantity();
			account.lockAmount(assetsSymbol, locked);
			order = createOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, SELL, orderType, orderDetails.getTime());
		}

		if (order != null) {
			orders.computeIfAbsent(order.getSymbol(), (s) -> new HashSet<>()).add(new PendingOrder(order, locked));
		}

		return order;
	}

	protected DefaultOrder createOrder(String assetsSymbol, String fundSymbol, double quantity, double price, Order.Side orderSide, Order.Type orderType, long closeTime) {
		DefaultOrder out = new DefaultOrder(assetsSymbol, fundSymbol, orderSide, closeTime);
		out.setPrice(BigDecimal.valueOf(price));
		out.setQuantity(BigDecimal.valueOf(quantity));
		out.setType(orderType);
		out.setStatus(Order.Status.NEW);
		out.setExecutedQuantity(BigDecimal.ZERO);
		out.setOrderId(UUID.randomUUID().toString());
		return out;
	}

	@Override
	public Map<String, Balance> updateBalances() {
		return account.getBalances();
	}

	public AccountManager getAccount() {
		return account;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return order;
	}

	@Override
	public void cancel(Order order) {
		order.cancel();
	}

	@Override
	public boolean isSimulated() {
		return true;
	}

	@Override
	public final synchronized boolean updateOpenOrders(String symbol, Candle candle) {
		Set<PendingOrder> s = orders.get(symbol);
		if (s == null || s.isEmpty()) {
			return false;
		}
		Iterator<PendingOrder> it = s.iterator();
		while (it.hasNext()) {
			PendingOrder pendingOrder = it.next();
			Order order = pendingOrder.order;

			if (!order.isFinalized()) {
				orderFillEmulator.fillOrder((DefaultOrder) order, candle);
			}
			if (order.isFinalized()) {
				it.remove();
				((DefaultOrder) order).setFeesPaid(new BigDecimal(getTradingFees().feesOnOrder(order)));
				updateBalances(order, pendingOrder.lockedAmount);
			}
		}
		return true;
	}

	private void updateBalances(Order order, BigDecimal locked) {
		final String asset = order.getAssetsSymbol();
		final String funds = order.getFundsSymbol();

		double amountTraded = order.getTotalTraded().doubleValue();
		double fees = amountTraded - getTradingFees().takeFee(amountTraded, order.getType(), order.getSide());

		if (order.isBuy()) {
			account.addToFreeBalance(asset, order.getExecutedQuantity());
			account.subtractFromLockedBalance(funds, locked);

			BigDecimal unspentAmount = locked.subtract(order.getTotalTraded());
			account.addToFreeBalance(funds, unspentAmount);
		} else if (order.isSell()) {
			account.subtractFromLockedBalance(asset, locked);
			account.addToFreeBalance(asset, order.getRemainingQuantity());
			account.addToFreeBalance(funds, order.getTotalTraded());
		}
		account.subtractFromFreeBalance(funds, new BigDecimal(fees));
	}
}


