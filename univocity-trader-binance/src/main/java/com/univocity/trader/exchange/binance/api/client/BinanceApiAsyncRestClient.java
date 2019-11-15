package com.univocity.trader.exchange.binance.api.client;

import com.univocity.trader.exchange.binance.api.client.domain.account.*;
import com.univocity.trader.exchange.binance.api.client.domain.account.request.*;
import com.univocity.trader.exchange.binance.api.client.domain.event.*;
import com.univocity.trader.exchange.binance.api.client.domain.general.*;
import com.univocity.trader.exchange.binance.api.client.domain.market.*;

import java.util.*;

/**
 * Binance API façade, supporting asynchronous/non-blocking access Binance's REST API.
 */
public interface BinanceApiAsyncRestClient {

	// General endpoints

	/**
	 * Test connectivity to the Rest API.
	 *
	 * @param callback the callback that handles the response
	 */
	void ping(BinanceApiCallback<Void> callback);

	/**
	 * Check server time.
	 */
	void getServerTime(BinanceApiCallback<ServerTime> callback);

	/**
	 * Current exchange trading rules and symbol information
	 *
	 * @param callback the callback that handles the response
	 */
	void getExchangeInfo(BinanceApiCallback<ExchangeInfo> callback);

	/**
	 * ALL supported assets and whether or not they can be withdrawn.
	 *
	 * @param callback the callback that handles the response
	 */
	void getAllAssets(BinanceApiCallback<List<Asset>> callback);

	// Market Data endpoints

	/**
	 * Get order book of a symbol (asynchronous)
	 *
	 * @param symbol   ticker symbol (e.g. ETHBTC)
	 * @param limit    depth of the order book (max 100)
	 * @param callback the callback that handles the response
	 */
	void getOrderBook(String symbol, Integer limit, BinanceApiCallback<OrderBook> callback);

	/**
	 * Get recent trades (up to last 500). Weight: 1
	 *
	 * @param symbol   ticker symbol (e.g. ETHBTC)
	 * @param limit    of last trades (Default 500; max 1000.)
	 * @param callback the callback that handles the response
	 */
	void getTrades(String symbol, Integer limit, BinanceApiCallback<List<TradeHistoryItem>> callback);

	/**
	 * Get older trades. Weight: 5
	 *
	 * @param symbol   ticker symbol (e.g. ETHBTC)
	 * @param limit    of last trades (Default 500; max 1000.)
	 * @param fromId   TradeId to fetch from. Default gets most recent trades.
	 * @param callback the callback that handles the response
	 */
	void getHistoricalTrades(String symbol, Integer limit, Long fromId, BinanceApiCallback<List<TradeHistoryItem>> callback);

	/**
	 * Get compressed, aggregate trades. Trades that fill at the time, from the same order, with
	 * the same price will have the quantity aggregated.
	 * If both <code>startTime</code> and <code>endTime</code> are sent, <code>limit</code>should not
	 * be sent AND the distance between <code>startTime</code> and <code>endTime</code> must be less than 24 hours.
	 *
	 * @param symbol    symbol to aggregate (mandatory)
	 * @param fromId    ID to get aggregate trades from INCLUSIVE (optional)
	 * @param limit     Default 500; max 1000 (optional)
	 * @param startTime Timestamp in ms to get aggregate trades from INCLUSIVE (optional).
	 * @param endTime   Timestamp in ms to get aggregate trades until INCLUSIVE (optional).
	 * @param callback  the callback that handles the response
	 *
	 * @return a list of aggregate trades for the given symbol
	 */
	void getAggTrades(String symbol, String fromId, Integer limit, Long startTime, Long endTime, BinanceApiCallback<List<AggTrade>> callback);

	/**
	 * Return the most recent aggregate trades for <code>symbol</code>
	 *
	 * @see #getAggTrades(String, String, Integer, Long, Long, BinanceApiCallback)
	 */
	void getAggTrades(String symbol, BinanceApiCallback<List<AggTrade>> callback);

	/**
	 * Kline/candlestick bars for a symbol. Klines are uniquely identified by their open time.
	 *
	 * @param symbol    symbol to aggregate (mandatory)
	 * @param interval  candlestick interval (mandatory)
	 * @param limit     Default 500; max 1000 (optional)
	 * @param startTime Timestamp in ms to get candlestick bars from INCLUSIVE (optional).
	 * @param endTime   Timestamp in ms to get candlestick bars until INCLUSIVE (optional).
	 * @param callback  the callback that handles the response containing a candlestick bar for the given symbol and interval
	 */
	void getCandlestickBars(String symbol, CandlestickInterval interval, Integer limit, Long startTime, Long endTime, BinanceApiCallback<List<Candlestick>> callback);

	/**
	 * Kline/candlestick bars for a symbol. Klines are uniquely identified by their open time.
	 *
	 * @see #getCandlestickBars(String, CandlestickInterval, BinanceApiCallback)
	 */
	void getCandlestickBars(String symbol, CandlestickInterval interval, BinanceApiCallback<List<Candlestick>> callback);

	/**
	 * Get 24 hour price change statistics (asynchronous).
	 *
	 * @param symbol   ticker symbol (e.g. ETHBTC)
	 * @param callback the callback that handles the response
	 */
	void get24HrPriceStatistics(String symbol, BinanceApiCallback<TickerStatistics> callback);

	/**
	 * Get 24 hour price change statistics for all symbols (asynchronous).
	 *
	 * @param callback the callback that handles the response
	 */
	void getAll24HrPriceStatistics(BinanceApiCallback<List<TickerStatistics>> callback);

	/**
	 * Get Latest price for all symbols (asynchronous).
	 *
	 * @param callback the callback that handles the response
	 */
	void getAllPrices(BinanceApiCallback<List<TickerPrice>> callback);

	/**
	 * Get latest price for <code>symbol</code> (asynchronous).
	 *
	 * @param symbol   ticker symbol (e.g. ETHBTC)
	 * @param callback the callback that handles the response
	 */
	void getPrice(String symbol, BinanceApiCallback<TickerPrice> callback);

	/**
	 * Get best price/qty on the order book for all symbols (asynchronous).
	 *
	 * @param callback the callback that handles the response
	 */
	void getBookTickers(BinanceApiCallback<List<BookTicker>> callback);

	// Account endpoints

	/**
	 * Send in a new order (asynchronous)
	 *
	 * @param order    the new order to submit.
	 * @param callback the callback that handles the response
	 */
	void newOrder(NewOrder order, BinanceApiCallback<NewOrderResponse> callback);

	/**
	 * Test new order creation and signature/recvWindow long. Creates and validates a new order but does not send it into the matching engine.
	 *
	 * @param order    the new TEST order to submit.
	 * @param callback the callback that handles the response
	 */
	void newOrderTest(NewOrder order, BinanceApiCallback<Void> callback);

	/**
	 * Check an order's status (asynchronous).
	 *
	 * @param orderStatusRequest order status request parameters
	 * @param callback           the callback that handles the response
	 */
	void getOrderStatus(OrderStatusRequest orderStatusRequest, BinanceApiCallback<Order> callback);

	/**
	 * Cancel an active order (asynchronous).
	 *
	 * @param cancelOrderRequest order status request parameters
	 * @param callback           the callback that handles the response
	 */
	void cancelOrder(CancelOrderRequest cancelOrderRequest, BinanceApiCallback<CancelOrderResponse> callback);

	/**
	 * Get all open orders on a symbol (asynchronous).
	 *
	 * @param orderRequest order request parameters
	 * @param callback     the callback that handles the response
	 */
	void getOpenOrders(OrderRequest orderRequest, BinanceApiCallback<List<Order>> callback);

	/**
	 * Get all account orders; active, canceled, or filled.
	 *
	 * @param orderRequest order request parameters
	 * @param callback     the callback that handles the response
	 */
	void getAllOrders(AllOrdersRequest orderRequest, BinanceApiCallback<List<Order>> callback);

	/**
	 * Get current account information (async).
	 */
	void getAccount(Long recvWindow, Long timestamp, BinanceApiCallback<Account> callback);

	/**
	 * Get current account information using default parameters (async).
	 */
	void getAccount(BinanceApiCallback<Account> callback);

	/**
	 * Get trades for a specific account and symbol.
	 *
	 * @param symbol   symbol to get trades from
	 * @param limit    default 500; max 1000
	 * @param fromId   TradeId to fetch from. Default gets most recent trades.
	 * @param callback the callback that handles the response with a list of trades
	 */
	void getMyTrades(String symbol, Integer limit, Long fromId, Long recvWindow, Long timestamp, BinanceApiCallback<List<Trade>> callback);

	/**
	 * Get trades for a specific account and symbol.
	 *
	 * @param symbol   symbol to get trades from
	 * @param limit    default 500; max 1000
	 * @param callback the callback that handles the response with a list of trades
	 */
	void getMyTrades(String symbol, Integer limit, BinanceApiCallback<List<Trade>> callback);

	/**
	 * Get trades for a specific account and symbol.
	 *
	 * @param symbol   symbol to get trades from
	 * @param callback the callback that handles the response with a list of trades
	 */
	void getMyTrades(String symbol, BinanceApiCallback<List<Trade>> callback);

	/**
	 * Submit a withdraw request.
	 * Enable Withdrawals option has to be active in the API settings.
	 *
	 * @param asset      asset symbol to withdraw
	 * @param address    address to withdraw to
	 * @param amount     amount to withdraw
	 * @param name       description/alias of the address
	 * @param addressTag Secondary address identifier for symbols like XRP,XMR etc.
	 */
	void withdraw(String asset, String address, String amount, String name, String addressTag, BinanceApiCallback<WithdrawResult> callback);

	/**
	 * Fetch account deposit history.
	 *
	 * @param callback the callback that handles the response and returns the deposit history
	 */
	void getDepositHistory(String asset, BinanceApiCallback<DepositHistory> callback);

	/**
	 * Fetch account withdraw history.
	 *
	 * @param callback the callback that handles the response and returns the withdraw history
	 */
	void getWithdrawHistory(String asset, BinanceApiCallback<WithdrawHistory> callback);

	/**
	 * Fetch deposit address.
	 *
	 * @param callback the callback that handles the response and returns the deposit address
	 */
	void getDepositAddress(String asset, BinanceApiCallback<DepositAddress> callback);

	// User stream endpoints

	/**
	 * Start a new user data stream.
	 *
	 * @param callback the callback that handles the response which contains a listenKey
	 */
	void startUserDataStream(BinanceApiCallback<ListenKey> callback);

	/**
	 * PING a user data stream to prevent a time out.
	 *
	 * @param listenKey listen key that identifies a data stream
	 * @param callback  the callback that handles the response which contains a listenKey
	 */
	void keepAliveUserDataStream(String listenKey, BinanceApiCallback<Void> callback);

	/**
	 * Close out a new user data stream.
	 *
	 * @param listenKey listen key that identifies a data stream
	 * @param callback  the callback that handles the response which contains a listenKey
	 */
	void closeUserDataStream(String listenKey, BinanceApiCallback<Void> callback);
}