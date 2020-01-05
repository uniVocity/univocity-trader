package com.univocity.trader.iqfeed;

import com.univocity.trader.*;
import com.univocity.trader.account.*;
import com.univocity.trader.iqfeed.api.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import org.asynchttpclient.*;
import org.slf4j.*;

import java.util.*;

class IQFeedClientAccount implements ClientAccount {

	private static final Logger log = LoggerFactory.getLogger(IQFeedApiWebSocketClient.class);

	private final IQFeedApiClientFactory factory;
	private final IQFeedApiWebSocketClient client;
	private SymbolPriceDetails symbolPriceDetails;
	private IQFeedExchange exchangeAPI;
	private double minimumBnbAmountToKeep = 1.0;

	public IQFeedClientAccount() {
		this.exchangeAPI = exchangeAPI;

		final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
		final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 655356);

		factory = IQFeedApiClientFactory.newInstance(asyncHttpClient);
		client = factory.newWebSocketClient();
	}

	// unused methods - ask about adding new interface for data vendor
	@Override
	public Order updateOrderStatus(Order order) {
		return null;
	}

	@Override
	public void cancel(Order order) {
	}

	@Override
	public OrderBook getOrderBook(String symbol, int depth) {
		return null;
	}

	@Override
	public Map<String, Balance> updateBalances() {
		return null;
	}

	@Override
	public Order executeOrder(OrderRequest orderDetails) {
		return null;
	}

	public double getMinimumBnbAmountToKeep() {
		return minimumBnbAmountToKeep;
	}

	public void setMinimumBnbAmountToKeep(double minimumBnbAmountToKeep) {
		this.minimumBnbAmountToKeep = minimumBnbAmountToKeep;
	}

	private SymbolPriceDetails getSymbolPriceDetails() {
		if (symbolPriceDetails == null) {
			symbolPriceDetails = new SymbolPriceDetails(exchangeAPI);
		}
		return symbolPriceDetails;
	}

}
