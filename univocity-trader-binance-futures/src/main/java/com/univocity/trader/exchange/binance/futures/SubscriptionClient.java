package com.univocity.trader.exchange.binance.futures;

import java.util.List;

import com.univocity.trader.exchange.binance.futures.impl.BinanceApiInternalFactory;
import com.univocity.trader.exchange.binance.futures.impl.BinanceFuturesApiWebSocketListener;
import com.univocity.trader.exchange.binance.futures.model.enums.CandlestickInterval;
import com.univocity.trader.exchange.binance.futures.model.event.AggregateTradeEvent;
import com.univocity.trader.exchange.binance.futures.model.event.CandlestickEvent;
import com.univocity.trader.exchange.binance.futures.model.event.LiquidationOrderEvent;
import com.univocity.trader.exchange.binance.futures.model.event.MarkPriceEvent;
import com.univocity.trader.exchange.binance.futures.model.event.OrderBookEvent;
import com.univocity.trader.exchange.binance.futures.model.event.SymbolBookTickerEvent;
import com.univocity.trader.exchange.binance.futures.model.event.SymbolMiniTickerEvent;
import com.univocity.trader.exchange.binance.futures.model.event.SymbolTickerEvent;
import com.univocity.trader.exchange.binance.futures.model.user.UserDataUpdateEvent;
import org.asynchttpclient.ws.WebSocket;

/***
 * The subscription client interface, it is used for subscribing any market data
 * update and account change, it is asynchronous, so you must implement the
 * SubscriptionListener interface. The server will push any update to the
 * client. if client get the update, the onReceive method will be called.
 */
public interface SubscriptionClient {
    /**
     * Create the subscription client to subscribe the update from server.
     *
     * @return The instance of synchronous client.

     */
    static SubscriptionClient create() {
        return create("", "", new SubscriptionOptions());
    }

    /**
     * Create the subscription client to subscribe the update from server.
     *
     * @param apiKey    The public key applied from Binance.
     * @param secretKey The private key applied from Binance.
     * @return The instance of synchronous client.
     */
    static SubscriptionClient create(String apiKey, String secretKey) {
        return BinanceApiInternalFactory.getInstance().createSubscriptionClient(apiKey, secretKey,
                new SubscriptionOptions());
    }

    /**
     * Create the subscription client to subscribe the update from server.
     *
     * @param apiKey              The public key applied from Binance.
     * @param secretKey           The private key applied from Binance.
     * @param subscriptionOptions The option of subscription connection, see
     *                            {@link SubscriptionOptions}
     * @return The instance of synchronous client.
     */
    static SubscriptionClient create(String apiKey, String secretKey, SubscriptionOptions subscriptionOptions) {
        return BinanceApiInternalFactory.getInstance().createSubscriptionClient(apiKey, secretKey, subscriptionOptions);
    }

    /**
     * Unsubscribe all subscription.
     */

    void unsubscribeAll();

    /**
     * Subscribe aggregate trade event. If the aggregate trade is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeAggregateTradeEvent(String symbol, BinanceFuturesApiCallback<AggregateTradeEvent> callback);

    /**
     * Subscribe mark price event. If the mark price is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeMarkPriceEvent(String symbol, BinanceFuturesApiCallback<MarkPriceEvent> callback);

    /**
     * Subscribe candlestick event. If the candlestick is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param interval      The candlestick interval, like "ONE_MINUTE".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeCandlestickEvent(String symbol, CandlestickInterval interval, BinanceFuturesApiCallback<CandlestickEvent> callback);

    /**
     * Subscribe individual symbol mini ticker event. If the symbol mini ticker is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeSymbolMiniTickerEvent(String symbol, BinanceFuturesApiCallback<SymbolMiniTickerEvent> callback);

    /**
     * Subscribe all market mini tickers event. If the mini tickers are updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeAllMiniTickerEvent(BinanceFuturesApiCallback<List<SymbolMiniTickerEvent>> callback);

    /**
     * Subscribe individual symbol ticker event. If the symbol ticker is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeSymbolTickerEvent(String symbol, BinanceFuturesApiCallback<SymbolTickerEvent> callback);

    /**
     * Subscribe all market tickers event. If the tickers are updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeAllTickerEvent(BinanceFuturesApiCallback<List<SymbolTickerEvent>> callback);

    /**
     * Subscribe individual symbol book ticker event. If the symbol book ticker is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeSymbolBookTickerEvent(String symbol, BinanceFuturesApiCallback<SymbolBookTickerEvent> callback);

    /**
     * Subscribe all market book tickers event. If the book tickers are updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeAllBookTickerEvent(BinanceFuturesApiCallback<SymbolBookTickerEvent> callback);

    /**
     * Subscribe individual symbol book ticker event. If the symbol book ticker is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeSymbolLiquidationOrderEvent(String symbol, BinanceFuturesApiCallback<LiquidationOrderEvent> callback);

    /**
     * Subscribe all market book tickers event. If the book tickers are updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeAllLiquidationOrderEvent(BinanceFuturesApiCallback<LiquidationOrderEvent> callback);

    /**
     * Subscribe partial book depth event. If the book depth is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param limit         The limit.
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeBookDepthEvent(String symbol, Integer limit, BinanceFuturesApiCallback<OrderBookEvent> callback);

    /**
     * Subscribe diff depth event. If the book depth is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "btcusdt".
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeDiffDepthEvent(String symbol, BinanceFuturesApiCallback<OrderBookEvent> callback);

    /**
     * Subscribe user data event. If the user data is updated,
     * server will send the data to client and onReceive in callback will be called.
     *
     * @param listenKey      The listenKey.
     * @param callback     The implementation is required. onReceive will be called
     *                     if receive server's update.
     */
    WebSocket subscribeUserDataEvent(String listenKey, BinanceFuturesApiCallback<UserDataUpdateEvent> callback);


}
