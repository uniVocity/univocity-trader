package com.univocity.trader.exchange.binance.futures;

import com.univocity.trader.ClientAccount;
import com.univocity.trader.SymbolPriceDetails;
import com.univocity.trader.TradingFees;
import com.univocity.trader.account.*;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.exchange.binance.futures.exception.*;
import com.univocity.trader.exchange.binance.futures.impl.BinanceApiInternalFactory;
import com.univocity.trader.exchange.binance.futures.model.enums.*;
import com.univocity.trader.exchange.binance.futures.model.market.*;
import com.univocity.trader.exchange.binance.futures.model.trade.AccountBalance;
import com.univocity.trader.exchange.binance.futures.model.trade.FuturesOrder;
import com.univocity.trader.exchange.binance.futures.model.trade.PositionRisk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.univocity.trader.account.Balance.roundStr;
import static com.univocity.trader.account.Order.Side.SELL;
import static com.univocity.trader.account.Order.Status.*;

class BinanceFuturesClientAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(BinanceFuturesClientAccount.class);

	private static final AtomicLong id = new AtomicLong(0);
	private final BinanceApiInternalFactory factory;
	private final SyncRequestClient client;
	private SymbolPriceDetails symbolPriceDetails;
	private BinanceFuturesExchange exchangeApi;
	private double minimumBnbAmountToKeep = 1.0;

	public BinanceFuturesClientAccount(String apiKey, String secret, BinanceFuturesExchange exchangeApi) {
		this.exchangeApi = exchangeApi;

		//final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
		//final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);
//		factory = BinanceApiClientFactory.newInstance(apiKey, secret, asyncHttpClient);
//		client = factory.newRestClient();

		factory = BinanceApiInternalFactory.getInstance();
		client = factory.createSyncRequestClient(apiKey, secret, new RequestOptions());
		new KeepAliveUserDataStream(client).start();
	}

	public double getMinimumBnbAmountToKeep() {
		return minimumBnbAmountToKeep;
	}

	public void setMinimumBnbAmountToKeep(double minimumBnbAmountToKeep) {
		this.minimumBnbAmountToKeep = minimumBnbAmountToKeep;
	}

	private SymbolPriceDetails getPriceDetails() {
		if (symbolPriceDetails == null) {
			symbolPriceDetails = new SymbolPriceDetails(exchangeApi);
		}
		return symbolPriceDetails;
	}

	@Override
	public TradingFees getTradingFees() {
		//TODO: fees may change depending on account and asset traded. Futures for example may cost 0.2%, or even 0.0%
		return SimpleTradingFees.percentage(0.1);
	}

	@Override
	public Order executeOrder(OrderRequest orderRequest) {
		String symbol = orderRequest.getSymbol();
		String price = roundStr(orderRequest.getPrice());
		String stopPrice = null;
		Order order = null;
		List<OrderRequest> attachments = orderRequest.attachedOrderRequests();

		if (orderRequest instanceof Order && ((Order) orderRequest).getAttachments() != null) {
			for (OrderRequest orderDetails : ((Order) orderRequest).getAttachments()) {

				if (((Order) orderDetails).isFinalized()) {
					// we've entered the loop again
					return (Order) orderDetails;
				}

				double amountToSpend = orderRequest.getTotalOrderAmount();

				if (orderDetails.getTriggerCondition() == Order.TriggerCondition.STOP_LOSS) {
					if (orderDetails.getSide() == SELL) {
						//as we are selling, fees will be taken out of the amount directly.
						orderRequest.setQuantity((amountToSpend / orderDetails.getPrice()));
					}

					// we did a SELL, now we're trying to buy, but we bet wrong
					stopPrice = roundStr(orderDetails.getPrice());


				} else {
					// we bet right
					if (orderDetails.getSide() == Order.Side.BUY) {
						orderRequest.setQuantity((amountToSpend / orderDetails.getPrice()));
					}
					orderRequest.setQuantity((amountToSpend / orderDetails.getPrice()));
					price = roundStr(orderDetails.getPrice());
				}
			}
		}


		BigDecimal finalPrice = new BigDecimal(price);
		BigDecimal finalStopPrice = stopPrice == null ? null : new BigDecimal(stopPrice);


		switch (orderRequest.getSide()) {
			case BUY:
				switch (orderRequest.getType()) {
					case LIMIT:

						if (stopPrice != null) {
							order = execute(orderRequest, q -> FuturesOrder.limitOCOSell(symbol, TimeInForce.GTC, q, finalPrice, finalStopPrice));
						} else {
							order = execute(orderRequest, q -> FuturesOrder.limitBuy(symbol, TimeInForce.GTC, q, finalPrice));
						}
						break;
					case MARKET:
						order = execute(orderRequest, q -> FuturesOrder.marketBuy(symbol, q));
						break;
				}
				break;
			case SELL:
				switch (orderRequest.getType()) {
					case LIMIT:
						if (stopPrice != null) {
							order = execute(orderRequest, q -> FuturesOrder.limitOCOBuy(symbol, TimeInForce.GTC, q, finalStopPrice, finalPrice));
						} else {
							order = execute(orderRequest, q -> FuturesOrder.limitSell(symbol, TimeInForce.GTC, q, finalPrice));
						}

						break;
					case MARKET:
						order = execute(orderRequest, q -> FuturesOrder.marketSell(symbol, q));
						break;
				}
		}


		if (attachments != null) {
			for (OrderRequest attachment : attachments) {
				Order o = createOrder(attachment);
				o.setParent(order);
			}
		}

		return order;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		com.univocity.trader.exchange.binance.futures.model.market.OrderBook book = client.getOrderBook(symbol, depth == 0 ? 5 : depth);
		OrderBook out = new OrderBook(this, symbol, depth);
		for (OrderBookEntry bid : book.getBids()) {
			out.addBid(bid.getPrice().doubleValue(), bid.getQty().doubleValue());
		}
		for (OrderBookEntry ask : book.getAsks()) {
			out.addAsk(ask.getPrice().doubleValue(), ask.getQty().doubleValue());
		}
		return out;
	}

	@Override
	public ConcurrentHashMap<String, Balance> updateBalances(boolean force) {
		List<AccountBalance> balances = client.getBalance();
		ConcurrentHashMap<String, Balance> out = new ConcurrentHashMap<>();
		for(AccountBalance b : balances) {
			String symbol = b.getAsset();
			Balance balance = new Balance(null, symbol);
			balance.setFree(b.getAvailableBalance().compareTo(BigDecimal.ZERO) < 0 ? 0 : b.getAvailableBalance().doubleValue());
			//balance.setLocked(b.getCrossUnPnl().doubleValue());
			//balance.setShorted(b.getCrossWalletBalance().doubleValue());
			out.put(symbol, balance);
		}

		//查询金本位合约持仓
		List<PositionRisk>  positionRisks = client.getPositionRisk();
		for(PositionRisk risk : positionRisks) {
			if(risk.getPositionAmt().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			String asset = risk.getSymbol().replace("USDT", "");
			Balance balance = null;
			if(out.containsKey(asset)) {
				balance = out.get(asset);
			} else {
				balance = new Balance(null, asset);
				out.put(asset, balance);
			}

			if(risk.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
				balance.setFree(risk.getPositionAmt().doubleValue());
			} else {
				balance.setShorted(Math.abs(risk.getPositionAmt().doubleValue()));
			}

		}
		return out;
	}

//	private Order translate(OrderRequest preparation, OrderDetails response) {
//		Order out = new Order(id.incrementAndGet(), preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(response.getSide()), Trade.Side.LONG, response.getTime());
//
//		out.setPrice(response.getAttachments().isEmpty() ? Double.parseDouble(response.getPrice()) : preparation.getPrice());
//		out.setAveragePrice(response.getAttachments().isEmpty() ? Double.parseDouble(response.getPrice()) : preparation.getPrice());
//		out.setQuantity(Double.parseDouble(response.getOrigQty()));
//		out.setExecutedQuantity(Double.parseDouble(response.getExecutedQty()));
//		out.setOrderId(String.valueOf(response.getOrderId()));
//		out.setStatus(translate(response.getStatus()));
//		out.setType(translate(response.getType()));
//
//		for (OrderDetails orderDetails : response.getAttachments()) {
//			Order o = createOrder(preparation, orderDetails);
//			out.setParent(o);
//		}
//		return out;
//	}

	private Order createOrder(OrderRequest request) {
		Order out = new Order(id.incrementAndGet(), request);
		initializeOrder(out, request.getPrice(), request.getQuantity(), request);
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

//	private Order createOrder(OrderRequest preparation, OrderDetails orderDetails) {
//		Order child = new Order(id.incrementAndGet(), preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(orderDetails.getSide()), Trade.Side.LONG, orderDetails.getTime());
//
//		child.setTriggerCondition(orderDetails.getType() == STOP_LOSS_LIMIT ? Order.TriggerCondition.STOP_LOSS : Order.TriggerCondition.STOP_GAIN, Double.parseDouble(orderDetails.getPrice()));
//		child.setPrice(Double.parseDouble(orderDetails.getPrice()));
//		child.setAveragePrice(Double.parseDouble(orderDetails.getPrice()));
//		child.setQuantity(Double.parseDouble(orderDetails.getOrigQty()));
//		child.setExecutedQuantity(Double.parseDouble(orderDetails.getExecutedQty()));
//		child.setOrderId(String.valueOf(orderDetails.getOrderId()));
//		child.setStatus(translate(orderDetails.getStatus()));
//		child.setType(translate(orderDetails.getType()));
//		return child;
//	}

	private Order.Side translate(OrderSide side) {
		switch (side) {
			case BUY:
				return Order.Side.BUY;
			case SELL:
				return SELL;
		}
		throw new IllegalStateException("Can't translate " + side + " to Order.Side");
	}
/*
	private Trade.Side translate(PositionSide side) {
		switch (side) {
			case SHORT:
				return Trade.Side.SHORT;
			case LONG:
				return Trade.Side.LONG;
		}
		throw new IllegalStateException("Can't translate " + side + " to Order.Side");
	}
	*/

	private Order.Status translate(OrderStatus status) {
		switch (status) {
			case EXPIRED:
			case CANCELED:
			case REJECTED:
				return Order.Status.CANCELLED;
			case NEW:
				return Order.Status.NEW;
			case FILLED:
				return FILLED;
			case PARTIALLY_FILLED:
				return Order.Status.PARTIALLY_FILLED;
		}
		throw new IllegalStateException("Can't translate " + status + " to Order.Status");
	}

	private Order.Type translate(OrderType type) {
		switch (type) {
			case LIMIT:
			case STOP_MARKET:
			case TAKE_PROFIT_MARKET:
				return Order.Type.LIMIT;
			case MARKET:
				return Order.Type.MARKET;
		}
		throw new IllegalStateException("Can't translate " + type + " to Order.Type");
	}


//
	private Order execute(OrderRequest orderPreparation, Function<BigDecimal, FuturesOrder> orderFunction) {
		if (orderPreparation.getSide() == SELL && orderPreparation.getAssetsSymbol().equalsIgnoreCase("BNB")) {
			double newQuantity = orderPreparation.getQuantity() - minimumBnbAmountToKeep;
			orderPreparation.setQuantity(newQuantity);
		}

		SymbolPriceDetails f = getPriceDetails().switchToSymbol(orderPreparation.getSymbol());
		if (orderPreparation.getTotalOrderAmount() > f.getMinimumOrderAmount(orderPreparation.getPrice())) {
			//NewOrder order = null;
			FuturesOrder order = null;
			try {
				BigDecimal qty = f.adjustQuantityScale(orderPreparation.getQuantity());
				order = orderFunction.apply(qty);
				log.info("Executing {} order: {}", order.getType(), order);

				//return translate(orderPreparation, order.getStopPrice() != null ? client.newOrderOCO(order.newOrderRespType(NewOrderResponseType.FULL)) : client.postOrder(order.newOrderRespType(NewOrderResponseType.FULL)));
				return translate(orderPreparation, client.postOrder(order, NewOrderRespType.ACK));
			} catch (BinanceApiException e) {
				log.error("Error processing order " + order, e);
			}
		}
		return null;
	}

	@Override
	public Order updateOrderStatus(Order order) {
		return translate(order, client.getOrder(order.getSymbol(), Long.valueOf(order.getOrderId()), null));
	}

	private Order translate(Order original, FuturesOrder order) {
		//Order out = new Order(original.getInternalId(), original.getAssetsSymbol(), original.getFundsSymbol(), translate(order.getSide()), translate(order.getPositionSide()), order.getUpdateTime());
		Order out = new Order(original.getInternalId(), original.getAssetsSymbol(), original.getFundsSymbol(), translate(order.getSide()), original.getTradeSide(), order.getUpdateTime());
		out.setStatus(translate(order.getStatus()));
		out.setExecutedQuantity(order.getExecutedQty().doubleValue());
		out.setAveragePrice(order.getPrice().doubleValue());
		out.setPrice(order.getPrice().doubleValue());
		out.setOrderId(String.valueOf(order.getOrderId()));
		out.setType(translate(order.getType()));
		out.setQuantity(order.getOrigQty().doubleValue());
		out.setTrade(original.getTrade());
		if (original.getAttachments() != null) {
			for (Order attachment : original.getAttachments()) {
				attachment.setParent(out);
			}
		}
		return out;
	}

	private Order translate(OrderRequest preparation, FuturesOrder order) {
		//Order out = new Order(id.incrementAndGet(), preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(order.getSide()), translate(order.getPositionSide()), order.getUpdateTime());
		Order out = new Order(id.incrementAndGet(), preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(order.getSide()), preparation.getTradeSide(), order.getUpdateTime());

		out.setPrice(order.getPrice().doubleValue());
		out.setAveragePrice(order.getPrice().doubleValue());
		out.setQuantity(order.getOrigQty().doubleValue());
		out.setExecutedQuantity(order.getExecutedQty().doubleValue());
		out.setOrderId(String.valueOf(order.getOrderId()));
		out.setStatus(translate(order.getStatus()));
		out.setType(translate(order.getType()));
		/*
		for (OrderDetails orderDetails : response.getAttachments()) {
			Order o = createOrder(preparation, orderDetails);
			out.setParent(o);
		}
		*/
		return out;
	}

	@Override
	public void cancel(Order order) {
		try {
			FuturesOrder futuresOrder = client.getOrder(order.getSymbol(), Long.valueOf(order.getOrderId()), null);
			if (OrderStatus.CANCELED.equals(futuresOrder.getStatus()) || OrderStatus.REJECTED.equals(futuresOrder.getStatus())) {
				log.info("Order {} was already cancelled or is pending cancellation", order);
			} else {
				FuturesOrder cancelledFuturesOrder = client.cancelOrder(order.getSymbol(), Long.valueOf(order.getOrderId()), null);
				log.info("Cancelled order {}. Response: {}", order, cancelledFuturesOrder);
			}
		} catch (BinanceApiException e) {
			if (!"Unknown order sent.".equals(e.getMessage())) {
				throw e;
			}
			log.debug("Attempted to cancel an order that was not found on Binance (order {})", order);
		}
	}

}
