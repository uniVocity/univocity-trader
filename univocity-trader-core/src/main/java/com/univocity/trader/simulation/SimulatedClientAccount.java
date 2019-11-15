package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;

import java.math.*;
import java.util.*;

import static com.univocity.trader.account.Order.Side.*;

public class SimulatedClientAccount implements ClientAccount {

	private final TradingFees tradingFees;
	private final AccountManager account;

	public SimulatedClientAccount(String referenceCurrencySymbol, TradingFees tradingFees) {
		this.tradingFees = tradingFees;
		this.account = new AccountManager(referenceCurrencySymbol, this);
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		String fundsSymbol = orderDetails.getFundsSymbol();
		String assetsSymbol = orderDetails.getAssetsSymbol();
		Order.Type orderType = orderDetails.getType();
		double unitPrice = orderDetails.getPrice().doubleValue();
		double orderAmount = orderDetails.getTotalOrderAmount().doubleValue();
		double availableFunds = account.getAmount(fundsSymbol);
		double availableAssets = account.getAmount(assetsSymbol);
		double quantity = orderDetails.getQuantity().doubleValue();
		double fees = orderAmount - tradingFees.takeFee(orderAmount, orderType, orderDetails.getSide());

		if (orderDetails.getSide() == BUY && availableFunds - fees >= orderAmount - 0.000000001) {

			availableFunds -= orderAmount;
			availableFunds -= fees;
			account.setAmount(fundsSymbol, availableFunds);

			quantity += account.getAmount(assetsSymbol);
			account.setAmount(assetsSymbol, quantity);

			return fillOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, BUY, orderType);

		} else if (orderDetails.getSide() == SELL && availableAssets >= quantity) {

			availableAssets -= quantity;
			account.setAmount(assetsSymbol, availableAssets);

			availableFunds += orderAmount;
			availableFunds -= fees;
			account.setAmount(fundsSymbol, availableFunds);

			return fillOrder(assetsSymbol, fundsSymbol, quantity, unitPrice, SELL, orderType);
		}
		return null;
	}

	private DefaultOrder fillOrder(String assetsSymbol, String fundSymbol, double quantity, double price, Order.Side orderSide, Order.Type orderType) {
		DefaultOrder out = new DefaultOrder(assetsSymbol, fundSymbol, orderSide);
		out.setPrice(new BigDecimal(price));
		out.setQuantity(new BigDecimal(quantity));
		out.setTime(System.currentTimeMillis());
		out.setType(orderType);
		out.setStatus(Order.Status.FILLED);
		out.setExecutedQuantity(new BigDecimal(quantity));
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
	public TradingFees getTradingFees() {
		return tradingFees;
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
}


