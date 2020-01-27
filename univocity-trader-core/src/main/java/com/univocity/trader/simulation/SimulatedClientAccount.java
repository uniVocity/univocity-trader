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
import static com.univocity.trader.config.Allocation.*;

public class SimulatedClientAccount implements ClientAccount {

	private Map<String, Set<PendingOrder>> orders = new HashMap<>();
	private TradingFees tradingFees;
	private final AccountManager account;
	private OrderFillEmulator orderFillEmulator;
	private final int marginReservePercentage;

	private static class PendingOrder {
		final Order order;
		final BigDecimal lockedAmount;

		public PendingOrder(Order order, BigDecimal lockedAmount) {
			this.order = order;
			this.lockedAmount = round(lockedAmount);
		}
	}

	public SimulatedClientAccount(AccountConfiguration<?> accountConfiguration, Simulation simulation) {
		this.marginReservePercentage = accountConfiguration.marginReservePercentage();
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
		BigDecimal unitPrice = orderDetails.getPrice();
		final BigDecimal orderAmount = orderDetails.getTotalOrderAmount();

		BigDecimal availableFunds = account.getPreciseAmount(fundsSymbol);
		BigDecimal availableAssets = account.getPreciseAmount(assetsSymbol);
		if (orderDetails.isShort()) {
			if (orderDetails.isBuy()) {
				availableFunds = availableFunds.add(account.getMarginReserve(fundsSymbol, assetsSymbol));
			} else if (orderDetails.isSell()) {
				availableAssets = account.getPreciseShortedAmount(assetsSymbol);
			}
		}


		BigDecimal quantity = orderDetails.getQuantity();
		double fees = orderAmount.doubleValue() - getTradingFees().takeFee(orderAmount.doubleValue(), orderType, orderDetails.getSide());

		BigDecimal locked = BigDecimal.ZERO;

		Order order = null;
		if (orderDetails.isBuy() && availableFunds.doubleValue() - fees >= orderAmount.doubleValue() - 0.000000001) {
			if (orderDetails.isLong()) {
				locked = orderDetails.getTotalOrderAmount();
				account.lockAmount(fundsSymbol, locked);
			}
			order = createOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, BUY, orderDetails.getTradeSide(), orderType, orderDetails.getTime());

		} else if (orderDetails.isSell()) {
			if (orderDetails.isLong()) {
				if (availableAssets.compareTo(quantity) >= 0) {
					locked = orderDetails.getQuantity();
					account.lockAmount(assetsSymbol, locked);
					order = createOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, SELL, orderDetails.getTradeSide(), orderType, orderDetails.getTime());
				}
			} else if (orderDetails.isShort()) {
				if (availableFunds.compareTo(orderAmount) >= 0) {
					locked = account.applyMarginReserve(orderDetails.getTotalOrderAmount()).subtract(orderDetails.getTotalOrderAmount());

					account.lockAmount(fundsSymbol, locked);
					order = createOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, SELL, orderDetails.getTradeSide(), orderType, orderDetails.getTime());
				}
			}

		}

		if (order != null) {
			orders.computeIfAbsent(order.getSymbol(), (s) -> new HashSet<>()).add(new PendingOrder(order, locked));
		}

		return order;
	}

	protected DefaultOrder createOrder(String assetsSymbol, String fundSymbol, BigDecimal quantity, BigDecimal price, Order.Side orderSide, Trade.Side tradeSide, Order.Type orderType, long closeTime) {
		DefaultOrder out = new DefaultOrder(assetsSymbol, fundSymbol, orderSide, tradeSide, closeTime);
		out.setPrice(price);
		out.setQuantity(quantity);
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
		updateOpenOrders(order.getSymbol(), null);
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

			if (candle != null && !order.isFinalized()) {
				orderFillEmulator.fillOrder((DefaultOrder) order, candle);
			}
			if (order.isFinalized()) {
				it.remove();
				((DefaultOrder) order).setFeesPaid(BigDecimal.valueOf(getTradingFees().feesOnOrder(order)));
				updateBalances(order, pendingOrder.lockedAmount, candle);
			}
		}
		return true;
	}

	private void updateMarginReserve(String assetSymbol, String fundSymbol, Candle candle) {
		Balance funds = account.getBalance(fundSymbol);
		BigDecimal reserved = funds.getMarginReserve(assetSymbol);

		BigDecimal shortedQuantity = account.getBalance(assetSymbol).getShorted();
		if (shortedQuantity.doubleValue() <= EFFECTIVELY_ZERO) {
			funds.setFree(funds.getFree().add(reserved));
			funds.setMarginReserve(assetSymbol, BigDecimal.ZERO);
		} else {
			double close;
			if (candle == null) {
				Trader trader = getAccount().getTraderOf(assetSymbol + fundSymbol);
				close = trader.lastClosingPrice();
			} else {
				close = candle.close;
			}

			BigDecimal newReserve = account.applyMarginReserve(shortedQuantity.multiply(BigDecimal.valueOf(close)));
			funds.setFree(funds.getFree().add(reserved).subtract(newReserve));
			funds.setMarginReserve(assetSymbol, newReserve);
		}
	}

	private void updateBalances(Order order, BigDecimal locked, Candle candle) {
		final String asset = order.getAssetsSymbol();
		final String funds = order.getFundsSymbol();

		double amountTraded = order.getTotalTraded().doubleValue();
		double fees = amountTraded - getTradingFees().takeFee(amountTraded, order.getType(), order.getSide());

		if (order.isBuy()) {
			if (order.isLong()) {
				account.addToFreeBalance(asset, order.getExecutedQuantity());
				account.subtractFromLockedBalance(funds, locked);

				BigDecimal unspentAmount = locked.subtract(order.getTotalTraded());
				account.addToFreeBalance(funds, unspentAmount);
			} else if (order.isShort()) {
				BigDecimal quantity = order.getExecutedQuantity();
				account.subtractFromShortedBalance(asset, quantity);
				account.subtractFromMarginReserveBalance(funds, asset, order.getTotalTraded());
				updateMarginReserve(asset, funds, candle);
			}
		} else if (order.isSell()) {
			if (order.isLong()) {
				account.subtractFromLockedBalance(asset, locked);
				account.addToFreeBalance(asset, order.getRemainingQuantity());
				account.addToFreeBalance(funds, order.getTotalTraded());
			} else if (order.isShort()) {
				account.subtractFromLockedBalance(funds, locked);
				account.addToFreeBalance(funds, locked);

				BigDecimal reserve = account.applyMarginReserve(order.getTotalTraded());
				account.subtractFromFreeBalance(funds, reserve.subtract(order.getTotalTraded()));
				account.addToMarginReserveBalance(funds, asset, reserve);
				account.addToShortedBalance(asset, order.getExecutedQuantity());
			}

		}
		account.subtractFromFreeBalance(funds, BigDecimal.valueOf(fees));
	}

	@Override
	public int marginReservePercentage() {
		return marginReservePercentage;
	}
}


