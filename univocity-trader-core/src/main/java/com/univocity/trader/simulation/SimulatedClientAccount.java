package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.orderfill.*;

import java.util.*;
import java.util.concurrent.*;

import static com.univocity.trader.config.Allocation.*;

public class SimulatedClientAccount implements ClientAccount {

	private Map<String, Set<Order>> orders = new HashMap<>();
	private TradingFees tradingFees;
	private final AccountManager account;
	private OrderFillEmulator orderFillEmulator;
	private final int marginReservePercentage;

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
		double unitPrice = orderDetails.getPrice();
		double orderAmount = orderDetails.getTotalOrderAmount();

		double availableFunds = account.getAmount(fundsSymbol);
		double availableAssets = account.getAmount(assetsSymbol);
		if (orderDetails.isShort()) {
			if (orderDetails.isBuy()) {
				availableFunds = availableFunds + account.getMarginReserve(fundsSymbol, assetsSymbol);
			} else if (orderDetails.isSell()) {
				availableAssets = account.getShortedAmount(assetsSymbol);
			}
		}

		double quantity = orderDetails.getQuantity();
		double fees = getTradingFees().feesOnOrder(orderDetails);
		boolean hasFundsAvailable = availableFunds - fees >= orderAmount - EFFECTIVELY_ZERO;
		if (!hasFundsAvailable && !orderDetails.isLongSell()) {
			double maxAmount = orderAmount - fees;
			double price = orderDetails.getPrice();
			if (price <= EFFECTIVELY_ZERO) {
				price = getAccount().getTraderOf(orderDetails.getSymbol()).lastClosingPrice();
			}
			quantity = (maxAmount / price) * 0.9999;
			orderDetails.setQuantity(quantity);

			double currentOrderAmount = orderDetails.getTotalOrderAmount();
			if (fees < currentOrderAmount && currentOrderAmount / maxAmount > 0.95) { //ensure we didn't create a very small order
				fees = getTradingFees().feesOnOrder(orderDetails);
				hasFundsAvailable = availableFunds - fees >= currentOrderAmount - EFFECTIVELY_ZERO;
				orderAmount = orderDetails.getTotalOrderAmount();
			}
		}


		DefaultOrder order = null;

		if (orderDetails.isBuy() && hasFundsAvailable) {
			if (orderDetails.isLong()) {
				account.lockAmount(fundsSymbol, orderAmount + fees);
			}
			order = createOrder(orderDetails, quantity, unitPrice);

		} else if (orderDetails.isSell()) {
			if (orderDetails.isLong()) {
				if (availableAssets < quantity) {
					double difference = 1.0 - (availableAssets / quantity);
					if (difference < 0.00001) { //0.001% quantity mismatch.
						quantity = availableAssets;
						orderDetails.setQuantity(quantity);
					}
				}
				if (availableAssets >= quantity) {
					account.lockAmount(assetsSymbol, orderDetails.getQuantity());
					order = createOrder(orderDetails, quantity, unitPrice);
				}
			} else if (orderDetails.isShort()) {
				if (hasFundsAvailable) {
					double locked = account.applyMarginReserve(orderAmount) - orderAmount;
					account.lockAmount(fundsSymbol, locked);
					order = createOrder(orderDetails, quantity, unitPrice);
				}
			}
		}

		if (order != null) {
			activateOrder(order);
			List<OrderRequest> attachments = orderDetails.attachedOrderRequests();
			if (attachments != null) {
				for (OrderRequest attachment : attachments) {
					DefaultOrder child = createOrder(attachment, attachment.getQuantity(), attachment.getPrice());
					child.setParent(order);
				}
			}
		}

		return order;
	}

	private void activateOrder(Order order) {
		orders.computeIfAbsent(order.getSymbol(), (s) -> new ConcurrentSkipListSet<>()).add(order);
	}

	private DefaultOrder createOrder(OrderRequest request, double quantity, double price) {
		DefaultOrder out = new DefaultOrder(request);
		initializeOrder(out, price, quantity, request);
		return out;
	}

	private void initializeOrder(DefaultOrder out, double price, double quantity, OrderRequest request) {
		out.setTriggerCondition(request.getTriggerCondition(), request.getTriggerPrice());
		out.setPrice(price);
		out.setQuantity(quantity);
		out.setType(request.getType());
		out.setStatus(Order.Status.NEW);
		out.setExecutedQuantity(0.0);
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

	private void activateAndTryFill(Candle candle, DefaultOrder order) {
		if (candle != null && order != null && !order.isCancelled()) {
			if (!order.isActive()) {
				if (triggeredBy(order, null, candle)) {
					order.activate();
				}
			}
			if (order.isActive()) {
				orderFillEmulator.fillOrder(order, candle);
			}
		}
	}

	@Override
	public final synchronized boolean updateOpenOrders(String symbol, Candle candle) {
		Set<Order> s = orders.get(symbol);
		if (s == null || s.isEmpty()) {
			return false;
		}
//		System.out.println("-------");

		Iterator<Order> it = s.iterator();
		while (it.hasNext()) {
			Order pendingOrder = it.next();
			DefaultOrder order = (DefaultOrder) pendingOrder;

			activateAndTryFill(candle, order);

			Order triggeredOrder = null;
			if (!order.isFinalized() && order.getFillPct() > 0.0) {
				//if attached order is triggered, cancel parent and submit triggered order.
				List<Order> attachments = order.getAttachments();
				if (attachments != null && !attachments.isEmpty()) {
					for (Order attachment : attachments) {
						if (triggeredBy(order, attachment, candle)) {
							triggeredOrder = attachment;
							break;
						}
					}

					if (triggeredOrder != null) {
						order.cancel();
					}
				}
			}

			order.setFeesPaid(order.getFeesPaid() + getTradingFees().feesOnPartialFill(order));

			if (order.isFinalized()) {
				it.remove();
				if (order.getParent() != null) { //order is child of a bracket order
					updateBalances(order, candle);
					for (Order attached : order.getParent().getAttachments()) { //cancel all open orders
						attached.cancel();
					}
				} else {
					updateBalances(order, candle);
				}

				List<Order> attachments = order.getAttachments();
				if (triggeredOrder == null && attachments != null && !attachments.isEmpty()) {
					for (Order attachment : attachments) {
						processAttachedOrder(order, (DefaultOrder) attachment, candle);
					}
				}
			} else if (order.hasPartialFillDetails()) {
				updateBalances(order, candle);
			}

			if (triggeredOrder != null && triggeredOrder.getQuantity() > 0) {
				processAttachedOrder(order, (DefaultOrder) triggeredOrder, candle);
			}
		}
		return true;
	}

	private void processAttachedOrder(DefaultOrder parent, DefaultOrder attached, Candle candle) {
		double locked = parent.getExecutedQuantity();
		if (locked > 0) {
			attached.updateTime(candle != null ? candle.openTime : parent.getTime());
			activateOrder(attached);
			account.waitForFill(attached);
			activateAndTryFill(candle, attached);
		}
	}


	private boolean triggeredBy(Order parent, Order attachment, Candle candle) {
		if (candle == null) {
			return false;
		}

		Double triggerPrice;
		Order.TriggerCondition trigger;
		if (attachment == null) {
			if (parent.getTriggerCondition() == Order.TriggerCondition.NONE) {
				return false;
			}
			triggerPrice = parent.getTriggerPrice();
			trigger = parent.getTriggerCondition();
		} else {
			triggerPrice = (attachment.getTriggerPrice() != 0.0) ? attachment.getTriggerPrice() : attachment.getPrice();
			trigger = attachment.getTriggerCondition();
		}
		if (triggerPrice == null) {
			return false;
		}

		double conditionalPrice = triggerPrice;

		switch (trigger) {
			case STOP_GAIN:
				return candle.low >= conditionalPrice;
			case STOP_LOSS:
				return candle.high <= conditionalPrice;
		}
		return false;
	}

	private void updateMarginReserve(DefaultOrder order, Candle candle) {
		final String assetSymbol = order.getAssetsSymbol();
		final String fundSymbol = order.getFundsSymbol();

		final Balance assets = account.getBalance(assetSymbol);
		final Balance funds = account.getBalance(fundSymbol);

		final double totalShorted = assets.getShorted();
		final double currentMarginReserve = funds.getMarginReserve(assetSymbol);

		double totalCovered = order.getPartialFillQuantity();

		double remainderBought = 0;
		double partialFillTotalPrice;
		if (totalCovered > totalShorted) { //bought to fully cover short and hold long position
			remainderBought = totalCovered - totalShorted;
			totalCovered -= remainderBought;

			partialFillTotalPrice = totalCovered * order.getPartialFillPrice();
		} else {
			partialFillTotalPrice = order.getPartialFillTotalPrice();
		}

		if (totalShorted > 0) {
			assets.setShorted(totalShorted - totalCovered);

			final double saleReserve = currentMarginReserve / account.marginReserveFactorPct();
			final double accountReserve = currentMarginReserve - saleReserve;

			final double close = candle != null ? candle.close : getAccount().getTraderOf(assetSymbol + fundSymbol).lastClosingPrice();
			final double shortSalePrice = close * (currentMarginReserve / account.applyMarginReserve(totalShorted * close));
			final double profit = shortSalePrice * totalCovered - partialFillTotalPrice - tradingFees.feesOnAmount(partialFillTotalPrice, order.getType(), order.getSide());

			double free = funds.getFree() + profit + accountReserve;

			if (assets.getShorted() <= EFFECTIVELY_ZERO) {
				funds.setFree(free);
				funds.setMarginReserve(assetSymbol, 0.0);
			} else {
				final double newReserve = account.applyMarginReserve(assets.getShorted() * shortSalePrice);
				final double newSaleReserve = newReserve / account.marginReserveFactorPct();
				final double newAccountReserve = newReserve - newSaleReserve;

				free = free - newAccountReserve;
				funds.setFree(free);
				funds.setMarginReserve(assetSymbol, newReserve);
			}
		}
		if (remainderBought > 0) {
			account.addToFreeBalance(assetSymbol, remainderBought);
			double total = remainderBought * order.getAveragePrice();
			account.subtractFromFreeBalance(fundSymbol, total + tradingFees.feesOnAmount(total, order.getType(), order.getSide()));

		}
	}

	private void updateBalances(DefaultOrder order, Candle candle) {
		final String asset = order.getAssetsSymbol();
		final String funds = order.getFundsSymbol();

		final double lastFillTotalPrice = order.getPartialFillTotalPrice();

		try {
			synchronized (account) {
				if (order.isBuy()) {
					if (order.isLong()) {
						if (order.getAttachments() != null) { //to be used by attached orders
							account.addToLockedBalance(asset, order.getPartialFillQuantity());
						} else {
							account.addToFreeBalance(asset, order.getPartialFillQuantity());
						}
						if (order.isFinalized()) {
							final double lockedFunds = order.getTotalOrderAmount();
							double unspentAmount = lockedFunds - order.getTotalTraded();

							if (unspentAmount != 0) {
								account.addToFreeBalance(funds, unspentAmount);
							}

							account.subtractFromLockedBalance(funds, lockedFunds);

							double maxFees = getTradingFees().feesOnTotalOrderAmount(order);
							account.subtractFromLockedBalance(funds, maxFees);
							account.addToFreeBalance(funds, maxFees - order.getFeesPaid());
						}
					} else if (order.isShort()) {
						if (order.hasPartialFillDetails()) {
							updateMarginReserve(order, candle);
						}
					}
				} else if (order.isSell()) {
					if (order.isLong()) {
						if (order.hasPartialFillDetails()) {
							double fee = tradingFees.feesOnAmount(order.getPartialFillTotalPrice(), order.getType(), order.getSide());
							account.addToFreeBalance(funds, lastFillTotalPrice - fee);
							account.subtractFromLockedBalance(asset, order.getPartialFillQuantity());
						}

						if (order.isFinalized()) {
							if (order.getParent() == null || order.getExecutedQuantity() > 0 || order.getParent().getAttachments().size() == 1 || allAttachedOrdersCancelled(order.getParent())) {
								account.addToFreeBalance(asset, order.getRemainingQuantity());
								account.subtractFromLockedBalance(asset, order.getRemainingQuantity());
							}
						}
					} else if (order.isShort()) {
						if (order.hasPartialFillDetails()) {
							double total = order.getPartialFillTotalPrice();
							double totalReserve = account.applyMarginReserve(total);
							double accountReserveAtCurrentPrice = totalReserve - total;

							double totalFilledAtOriginalPrice = order.getPrice() * order.getPartialFillQuantity();
							double accountReserveAtOriginalPrice = (account.applyMarginReserve(totalFilledAtOriginalPrice) - totalFilledAtOriginalPrice);

							double additionalFundsRequired = 0.0;

							if (accountReserveAtOriginalPrice < accountReserveAtCurrentPrice) {
								double extra = accountReserveAtCurrentPrice - accountReserveAtOriginalPrice;
								additionalFundsRequired += extra;
							}
							account.subtractFromLockedBalance(funds, accountReserveAtOriginalPrice);
							account.addToMarginReserveBalance(funds, asset, totalReserve - additionalFundsRequired - tradingFees.feesOnPartialFill(order));
							account.addToShortedBalance(asset, order.getPartialFillQuantity());
						}

						if (order.isFinalized()) {
							double totalTraded = order.getTotalTraded();
							double totalReserve = account.applyMarginReserve(order.getTotalOrderAmount());
							double unusedReserve = totalReserve - account.applyMarginReserve(totalTraded);
							if (unusedReserve > 0) {
								double unusedFunds = unusedReserve - (order.getTotalOrderAmountAtAveragePrice() - totalTraded);
								account.releaseFromLockedBalance(funds, unusedFunds);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error updating balances from order " + order, e);
		} finally {
			order.clearPartialFillDetails();
		}
	}

	private Order processedParent;

	private boolean allAttachedOrdersCancelled(Order order) {
		if (order == processedParent) {
			return false;
		}
		this.processedParent = order;
		for (Order o : order.getAttachments()) {
			if (o.getExecutedQuantity() > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int marginReservePercentage() {
		return marginReservePercentage;
	}
}


