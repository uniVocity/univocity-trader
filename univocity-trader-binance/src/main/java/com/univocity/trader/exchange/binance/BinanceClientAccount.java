package com.univocity.trader.exchange.binance;

import com.univocity.trader.*;
import com.univocity.trader.account.Order;
import com.univocity.trader.account.*;
import com.univocity.trader.account.OrderBook;
import com.univocity.trader.account.OrderRequest;
import com.univocity.trader.exchange.binance.api.client.*;
import com.univocity.trader.exchange.binance.api.client.domain.*;
import com.univocity.trader.exchange.binance.api.client.domain.account.*;
import com.univocity.trader.exchange.binance.api.client.domain.account.request.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;
import com.univocity.trader.exchange.binance.api.client.exception.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import org.asynchttpclient.*;
import org.slf4j.*;

import java.math.*;
import java.util.*;
import java.util.function.*;

import static com.univocity.trader.account.Order.Side.*;
import static com.univocity.trader.account.Order.Status.*;
import static com.univocity.trader.exchange.binance.api.client.domain.TimeInForce.*;
import static com.univocity.trader.exchange.binance.api.client.domain.account.NewOrder.*;

class BinanceClientAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(BinanceApiRestClient.class);

	private final BinanceApiClientFactory factory;
	private final BinanceApiRestClient client;
	private SymbolPriceDetails symbolPriceDetails;
	private BinanceExchange exchangeApi;
	private double minimumBnbAmountToKeep = 1.0;

	public BinanceClientAccount(String apiKey, String secret, BinanceExchange exchangeApi) {
		this.exchangeApi = exchangeApi;

		final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
		final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);

		factory = BinanceApiClientFactory.newInstance(apiKey, secret, asyncHttpClient);
		client = factory.newRestClient();
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
	public Order executeOrder(OrderRequest orderDetails) {
		String symbol = orderDetails.getSymbol();
		String price = orderDetails.getPrice().toPlainString();
		switch (orderDetails.getSide()) {
			case BUY:
				switch (orderDetails.getType()) {
					case LIMIT:
						return execute(orderDetails, q -> limitBuy(symbol, GTC, q, price));
					case MARKET:
						return execute(orderDetails, q -> marketBuy(symbol, q));
				}
			case SELL:
				switch (orderDetails.getType()) {
					case LIMIT:
						return execute(orderDetails, q -> limitSell(symbol, GTC, q, price));
					case MARKET:
						return execute(orderDetails, q -> marketSell(symbol, q));
				}
		}
		return null;
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		com.univocity.trader.exchange.binance.api.client.domain.market.OrderBook book = client.getOrderBook(symbol, depth == 0 ? 5 : depth);

		OrderBook out = new OrderBook(this, symbol, depth);
		for (OrderBookEntry bid : book.getBids()) {
			out.addBid(Double.parseDouble(bid.getPrice()), Double.parseDouble(bid.getQty()));
		}
		for (OrderBookEntry ask : book.getAsks()) {
			out.addAsk(Double.parseDouble(ask.getPrice()), Double.parseDouble(ask.getQty()));
		}
		return out;
	}

	@Override
	public synchronized Map<String, Balance> updateBalances() {
		Account account = client.getAccount();
		List<AssetBalance> balances = account.getBalances();

		Map<String, Balance> out = new HashMap<>();
		for (AssetBalance b : balances) {
			String symbol = b.getAsset();
			Balance balance = new Balance(symbol);
			balance.setFree(new BigDecimal(b.getFree()));
			balance.setLocked(new BigDecimal(b.getLocked()));
			out.put(symbol, balance);
		}
		return out;
	}

	private Order translate(OrderRequest preparation, OrderDetails response) {
		DefaultOrder out = new DefaultOrder(preparation.getAssetsSymbol(), preparation.getFundsSymbol(), translate(response.getSide()));

		out.setPrice(new BigDecimal(response.getPrice()));
		out.setQuantity(new BigDecimal(response.getOrigQty()));
		out.setExecutedQuantity(new BigDecimal(response.getExecutedQty()));
		out.setOrderId(String.valueOf(response.getOrderId()));
		out.setStatus(translate(response.getStatus()));
		out.setType(translate(response.getType()));
		out.setTime(response.getTime());
		return out;
	}

	private Order.Side translate(OrderSide side) {
		switch (side) {
			case BUY:
				return Order.Side.BUY;
			case SELL:
				return SELL;
		}
		throw new IllegalStateException("Can't translate " + side + " to Order.Side");
	}

	private Order.Status translate(OrderStatus status) {
		switch (status) {
			case EXPIRED:
			case CANCELED:
			case PENDING_CANCEL:
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
				return Order.Type.LIMIT;
			case MARKET:
				return Order.Type.MARKET;
		}
		throw new IllegalStateException("Can't translate " + type + " to Order.Type");
	}

	private Order execute(OrderRequest orderPreparation, Function<String, NewOrder> orderFunction) {
		if (orderPreparation.getSide() == SELL && orderPreparation.getAssetsSymbol().equalsIgnoreCase("BNB")) {
			BigDecimal newQuantity = orderPreparation.getQuantity().subtract(new BigDecimal(minimumBnbAmountToKeep));
			orderPreparation.setQuantity(newQuantity);
		}

		SymbolPriceDetails f = getPriceDetails().switchToSymbol(orderPreparation.getSymbol());
		if (orderPreparation.getTotalOrderAmount().compareTo(f.getMinimumOrderAmount(orderPreparation.getPrice())) > 0) {
			NewOrder order = null;
			try {
				BigDecimal qty = f.adjustQuantityScale(orderPreparation.getQuantity());
				order = orderFunction.apply(qty.toPlainString());
				log.info("Executing {} order: {}", order.getType(), order);

				return translate(orderPreparation, client.newOrder(order.newOrderRespType(NewOrderResponseType.FULL)));
			} catch (BinanceApiException e) {
				log.error("Error processing order " + order, e);
			}
		}
		return null;
	}

	@Override
	public Order updateOrderStatus(Order order) {
		OrderStatusRequest request = new OrderStatusRequest(order.getSymbol(), Long.valueOf(order.getOrderId()));
		return translate(order, client.getOrderStatus(request));
	}

	private Order translate(Order original, com.univocity.trader.exchange.binance.api.client.domain.account.Order order) {
		DefaultOrder out = new DefaultOrder(original.getAssetsSymbol(), original.getFundsSymbol(), translate(order.getSide()));
		out.setStatus(translate(order.getStatus()));
		out.setExecutedQuantity(new BigDecimal(order.getExecutedQty()));
		out.setPrice(new BigDecimal(order.getPrice()));
		out.setOrderId(String.valueOf(order.getOrderId()));
		out.setType(translate(order.getType()));
		out.setQuantity(new BigDecimal(order.getOrigQty()));
		out.setTime(order.getTime());
		return out;
	}

	@Override
	public void cancel(Order order) {
		CancelOrderResponse response = client.cancelOrder(new CancelOrderRequest(order.getSymbol(), Long.valueOf(order.getOrderId())));
		log.info("Cancelled order {}. Response: {}", order, response);
	}
}
