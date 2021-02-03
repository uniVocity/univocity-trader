package com.univocity.trader.simulation;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.config.*;
import com.univocity.trader.simulation.orderfill.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import static com.univocity.trader.config.Allocation.*;

public class SimulatedClientAccount implements ClientAccount {

	private final AtomicLong orderIdGenerator = new AtomicLong(0);
	private TradingFees tradingFees;
	protected SimulatedAccountManager accountManager;
	private final OrderFillEmulator orderFillEmulator;
	private final int marginReservePercentage;

	public SimulatedClientAccount(AccountConfiguration<?> accountCfg, Simulation simulationCfg, Supplier<SignalRepository> signalRepository) {
		this(accountCfg, simulationCfg.orderFillEmulator(), simulationCfg.tradingFees(), signalRepository);
	}

	public SimulatedClientAccount(AccountConfiguration<?> accountConfiguration, OrderFillEmulator orderFillEmulator, TradingFees tradingFees, Supplier<SignalRepository> signalRepository) {
		this.marginReservePercentage = accountConfiguration.marginReservePercentage();
		this.accountManager = new SimulatedAccountManager(this, accountConfiguration, tradingFees, signalRepository);
		this.orderFillEmulator = orderFillEmulator;
	}

	public final TradingFees getTradingFees() {
		if (this.tradingFees == null) {
			this.tradingFees = accountManager.getTradingFees();
			if (this.tradingFees == null) {
				throw new IllegalArgumentException("Trading fees cannot be null");
			}
		}
		return this.tradingFees;
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		String fundsSymbol = orderDetails.getFundsSymbol();
		String assetsSymbol = orderDetails.getAssetsSymbol();
		Order.Type orderType = orderDetails.getType();
		double unitPrice = orderDetails.getPrice();
		double orderAmount = orderDetails.getTotalOrderAmount();

		double availableFunds = accountManager.getAmount(fundsSymbol);
		double availableAssets = accountManager.getAmount(assetsSymbol);
		if (orderDetails.isShort()) {
			if (orderDetails.isBuy()) {
				availableFunds = availableFunds + accountManager.getMarginReserve(fundsSymbol, assetsSymbol);
			} else if (orderDetails.isSell()) {
				availableAssets = accountManager.getShortedAmount(assetsSymbol);
			}
		}

		double quantity = orderDetails.getQuantity();
		double fees = getTradingFees().feesOnOrder(orderDetails);
		boolean hasFundsAvailable = availableFunds - fees >= orderAmount - EFFECTIVELY_ZERO;
		if (!hasFundsAvailable && !orderDetails.isLongSell()) {
			double maxAmount = orderAmount - fees;
			double price = orderDetails.getPrice();
			if (price <= EFFECTIVELY_ZERO) {
				price = getAccount().getLatestPrice(orderDetails.getSymbol());
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


		Order order = null;

		if (orderDetails.isBuy() && hasFundsAvailable) {
			if (orderDetails.isLong() && !orderDetails.isMarket()) {
				accountManager.lockAmount(fundsSymbol, orderAmount + fees);
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
					accountManager.lockAmount(assetsSymbol, orderDetails.getQuantity());
					order = createOrder(orderDetails, quantity, unitPrice);
				}
			} else if (orderDetails.isShort()) {
				if (hasFundsAvailable) {
					double locked = accountManager.applyMarginReserve(orderAmount) - orderAmount;
					accountManager.lockAmount(fundsSymbol, locked);
					order = createOrder(orderDetails, quantity, unitPrice);
				}
			}
		}

		if (order != null) {
			List<OrderRequest> attachments = orderDetails.attachedOrderRequests();
			if (attachments != null) {
				for (OrderRequest attachment : attachments) {
					Order child = createOrder(attachment, attachment.getQuantity(), attachment.getPrice());
					child.setParent(order);
				}
			}
		}

		return order;
	}

	private Order createOrder(OrderRequest request, double quantity, double price) {
		Order out = new Order(orderIdGenerator.incrementAndGet(), request);
		initializeOrder(out, price, quantity, request);
		return out;
	}

	private void initializeOrder(Order out, double price, double quantity, OrderRequest request) {
		out.setTriggerCondition(request.getTriggerCondition(), request.getTriggerPrice());
		out.setPrice(price);
		out.setQuantity(quantity);
		out.setType(request.getType());
		out.setStatus(Order.Status.NEW);
		out.setExecutedQuantity(0.0);
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances(boolean force) {
		return accountManager.getBalances();
	}

	public SimulatedAccountManager getAccount() {
		return accountManager;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	@Override
	public void cancel(Order order) {
		order.cancel();
		updateOpenOrder(order);
	}

	@Override
	public boolean isSimulated() {
		return true;
	}

	private void activateAndTryFill(Candle candle, Order order) {
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
	public Order updateOrderStatus(Order order) {
		updateOpenOrder(order);
		return order;
	}


	private final void updateOpenOrder(Order order) {
		if (order.processed) {
			return;
		}
		Candle candle = order.getTrade().latestCandle();
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
			order.processed = true;
			if (order.getParent() != null) { //order is child of a bracket order
				for (Order attached : order.getParent().getAttachments()) { //cancel all open orders
					attached.cancel();
				}
			}
			updateBalances(order, candle);

			List<Order> attachments = order.getAttachments();
			if (triggeredOrder == null && attachments != null && !attachments.isEmpty()) {
				for (Order attachment : attachments) {
					processAttachedOrder(order, attachment, candle);
				}
			}
		} else if (order.hasPartialFillDetails()) {
			updateBalances(order, candle);
		}

		if (triggeredOrder != null && triggeredOrder.getQuantity() > 0) {
			processAttachedOrder(order, triggeredOrder, candle);
		}
	}

	private void processAttachedOrder(Order parent, Order attached, Candle candle) {
		double locked = parent.getExecutedQuantity();
		if (locked > 0) {
			attached.updateTime(candle != null ? candle.openTime : parent.getTime());
			parent.getTrade().trader().tradingManager.waitForFill(attached);
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
		if (triggerPrice == 0.0) {
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

	private void updateMarginReserve(Order order, Candle candle) {
		final String assetSymbol = order.getAssetsSymbol();
		final String fundSymbol = order.getFundsSymbol();

		final Balance assets = accountManager.getBalance(assetSymbol);
		final Balance funds = accountManager.getBalance(fundSymbol);

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

			final double saleReserve = currentMarginReserve / accountManager.marginReserveFactorPct();
			final double accountReserve = currentMarginReserve - saleReserve;

			final double close = candle != null ? candle.close : getAccount().getLatestPrice(assetSymbol + fundSymbol);
			final double shortSalePrice = close * (currentMarginReserve / accountManager.applyMarginReserve(totalShorted * close));
			final double profit = shortSalePrice * totalCovered - partialFillTotalPrice - tradingFees.feesOnAmount(partialFillTotalPrice, order.getType(), order.getSide());

			double free = funds.getFree() + profit + accountReserve;

			if (assets.getShorted() <= EFFECTIVELY_ZERO) {
				funds.setFree(free);
				funds.setMarginReserve(assetSymbol, 0.0);
			} else {
				final double newReserve = accountManager.applyMarginReserve(assets.getShorted() * shortSalePrice);
				final double newSaleReserve = newReserve / accountManager.marginReserveFactorPct();
				final double newAccountReserve = newReserve - newSaleReserve;

				free = free - newAccountReserve;
				funds.setFree(free);
				funds.setMarginReserve(assetSymbol, newReserve);
			}
		}
		if (remainderBought > 0) {
			accountManager.addToFreeBalance(assetSymbol, remainderBought);
			double total = remainderBought * order.getAveragePrice();
			accountManager.subtractFromFreeBalance(fundSymbol, total + tradingFees.feesOnAmount(total, order.getType(), order.getSide()));

		}
	}

	private void updateBalances(Order order, Candle candle) {
		final String asset = order.getAssetsSymbol();
		final String funds = order.getFundsSymbol();

		final double lastFillTotalPrice = order.getPartialFillTotalPrice();

		try {
			if (order.isBuy()) {
				if (order.isLong()) {
					if (order.isFinalized()) {
						if (!order.isMarket()) {
							final double lockedFunds = order.getTotalOrderAmount();
							double unspentAmount = lockedFunds - order.getTotalTraded();

							if (unspentAmount != 0) {
								accountManager.addToFreeBalance(funds, unspentAmount);
							}

							accountManager.subtractFromLockedBalance(funds, lockedFunds);

							double maxFees = getTradingFees().feesOnTotalOrderAmount(order);
							accountManager.subtractFromLockedBalance(funds, maxFees);
							accountManager.addToFreeBalance(funds, maxFees - order.getFeesPaid());
						} else {
							double totalCost = order.getTotalOrderAmount() + order.getFeesPaid();
							if (accountManager.getBalance(order.getFundsSymbol()).getFree() > totalCost) {
								accountManager.subtractFromFreeBalance(funds, totalCost);
							} else {
								order.setStatus(Order.Status.CANCELLED);
								order.setPartialFillDetails(0, 0);
								order.setExecutedQuantity(0);
								order.setFeesPaid(0);
								order.setAveragePrice(0);
							}
						}
					}
					if (order.getAttachments() != null) { //to be used by attached orders
						accountManager.addToLockedBalance(asset, order.getPartialFillQuantity());
					} else {
						accountManager.addToFreeBalance(asset, order.getPartialFillQuantity());
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
						accountManager.addToFreeBalance(funds, lastFillTotalPrice - fee);
						accountManager.subtractFromLockedBalance(asset, order.getPartialFillQuantity());
					}

					if (order.isFinalized()) {
						if (order.getParent() == null || order.getExecutedQuantity() > 0 || order.getParent().getAttachments().size() == 1 || allAttachedOrdersCancelled(order.getParent())) {
							accountManager.addToFreeBalance(asset, order.getRemainingQuantity());
							accountManager.subtractFromLockedBalance(asset, order.getRemainingQuantity());
						}
					}
				} else if (order.isShort()) {
					if (order.hasPartialFillDetails()) {
						double total = order.getPartialFillTotalPrice();
						double totalReserve = accountManager.applyMarginReserve(total);
						double accountReserveAtCurrentPrice = totalReserve - total;

						double totalFilledAtOriginalPrice = order.getPrice() * order.getPartialFillQuantity();
						double accountReserveAtOriginalPrice = (accountManager.applyMarginReserve(totalFilledAtOriginalPrice) - totalFilledAtOriginalPrice);

						double additionalFundsRequired = 0.0;

						if (accountReserveAtOriginalPrice < accountReserveAtCurrentPrice) {
							double extra = accountReserveAtCurrentPrice - accountReserveAtOriginalPrice;
							additionalFundsRequired += extra;
						}
						accountManager.subtractFromLockedBalance(funds, accountReserveAtOriginalPrice);
						accountManager.addToMarginReserveBalance(funds, asset, totalReserve - additionalFundsRequired - tradingFees.feesOnPartialFill(order));
						accountManager.addToShortedBalance(asset, order.getPartialFillQuantity());
					}

					if (order.isFinalized()) {
						double totalTraded = order.getTotalTraded();
						double totalReserve = accountManager.applyMarginReserve(order.getTotalOrderAmount());
						double unusedReserve = totalReserve - accountManager.applyMarginReserve(totalTraded);
						if (unusedReserve > 0) {
							double unusedFunds = unusedReserve - (order.getTotalOrderAmountAtAveragePrice() - totalTraded);
							accountManager.releaseFromLockedBalance(funds, unusedFunds);
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


