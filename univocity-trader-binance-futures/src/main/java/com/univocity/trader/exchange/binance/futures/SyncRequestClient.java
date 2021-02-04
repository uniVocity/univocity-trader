package com.univocity.trader.exchange.binance.futures;

import com.alibaba.fastjson.JSONObject;
import com.univocity.trader.exchange.binance.futures.impl.BinanceApiInternalFactory;
import com.univocity.trader.exchange.binance.futures.model.ResponseResult;
import com.univocity.trader.exchange.binance.futures.exception.*;
import com.univocity.trader.exchange.binance.futures.model.enums.*;
import com.univocity.trader.exchange.binance.futures.model.market.*;
import com.univocity.trader.exchange.binance.futures.model.trade.*;

import java.util.List;

/**
 * Synchronous request interface, invoking Binance RestAPI via synchronous
 * method.<br>
 * All methods in this interface will be blocked until the RestAPI response.
 * <p>
 * If the invoking failed or timeout, the
 * {@link BinanceApiException} will be thrown.
 */
public interface SyncRequestClient {

    /**
     * Create the synchronous client. All interfaces defined in synchronous client
     * are implemented by synchronous mode.
     *
     * @return The instance of synchronous client.
     */
    static SyncRequestClient create() {
        return create("", "", new RequestOptions());
    }

    /**
     * Create the synchronous client. All interfaces defined in synchronous client
     * are implemented by synchronous mode.
     *
     * @param apiKey    The public key applied from binance.
     * @param secretKey The private key applied from binance.
     * @return The instance of synchronous client.
     */
    static SyncRequestClient create(String apiKey, String secretKey) {
        return BinanceApiInternalFactory.getInstance().createSyncRequestClient(apiKey, secretKey, new RequestOptions());
    }

    /**
     * Create the synchronous client. All interfaces defined in synchronous client
     * are implemented by synchronous mode.
     *
     * @param apiKey    The public key applied from binance.
     * @param secretKey The private key applied from binance.
     * @param options   The request option.
     * @return The instance of synchronous client.
     */
    static SyncRequestClient create(String apiKey, String secretKey, RequestOptions options) {
        return BinanceApiInternalFactory.getInstance().createSyncRequestClient(apiKey, secretKey, options);
    }


    /**
     * Fetch current exchange trading rules and symbol information.
     *
     * @return Current exchange trading rules and symbol information.
     */
    ExchangeInformation getExchangeInformation();

    /**
     * Fetch order book.
     *
     * @return Order book.
     */
    OrderBook getOrderBook(String symbol, Integer limit);

    /**
     * Get recent trades.
     *
     * @return Recent trades.
     */
    List<Trade> getRecentTrades(String symbol, Integer limit);

    /**
     * Get old Trade.
     *
     * @return Old trades.
     */
    List<Trade> getOldTrades(String symbol, Integer limit, Long fromId);

    /**
     * Get compressed, aggregate trades.
     *
     * @return Aggregate trades.
     */
    List<AggregateTrade> getAggregateTrades(String symbol, Long fromId, Long startTime, Long endTime, Integer limit);

    /**
     * Get kline/candlestick bars for a symbol.
     *
     * @return Kline/candlestick bars for a symbol.
     */
    List<Candlestick> getCandlestick(String symbol, CandlestickInterval interval, Long startTime, Long endTime, Integer limit);

    /**
     * Get mark price for a symbol.
     *
     * @return Mark price for a symbol.
     */
    List<MarkPrice> getMarkPrice(String symbol);

    /**
     * Get funding rate history.
     *
     * @return funding rate history.
     */
    List<FundingRate> getFundingRate(String symbol, Long startTime, Long endTime, Integer limit);

    /**
     * Get 24 hour rolling window price change statistics.
     *
     * @return 24 hour rolling window price change statistics.
     */
    List<PriceChangeTicker> get24hrTickerPriceChange(String symbol);

    /**
     * Get latest price for a symbol or symbols.
     *
     * @return Latest price for a symbol or symbols.
     */
    List<SymbolPrice> getSymbolPriceTicker(String symbol);

    /**
     * Get best price/qty on the order book for a symbol or symbols.
     *
     * @return Best price/qty on the order book for a symbol or symbols.
     */
    List<SymbolOrderBook> getSymbolOrderBookTicker(String symbol);

    /**
     * Get all liquidation orders.
     *
     * @return All liquidation orders.
     */
    List<LiquidationOrder> getLiquidationOrders(String symbol, Long startTime, Long endTime, Integer limit);

    /**
     * Place new orders
     * @param batchOrders
     * @return
     */
    List<Object> postBatchOrders(String batchOrders);
    
    /**
     * Send in a new order.
     *
     * @return Order.

    Order postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType,
					TimeInForce timeInForce, String quantity, String price, String reduceOnly,
					String newClientOrderId, String stopPrice, WorkingType workingType, NewOrderRespType newOrderRespType);
     */
    FuturesOrder postOrder(FuturesOrder order, NewOrderRespType newOrderRespType);

    /**
     * Cancel an active order.
     *
     * @return Order.
     */
    FuturesOrder cancelOrder(String symbol, Long orderId, String origClientOrderId);

    /**
     * Cancel all open orders.
     *
     * @return ResponseResult.
     */
    ResponseResult cancelAllOpenOrder(String symbol);

    /**
     * Batch cancel orders.
     *
     * @return Order.
     */
    List<Object> batchCancelOrders(String symbol, String orderIdList, String origClientOrderIdList);

    /**
     * Switch position side. (true == dual, false == both)
     *
     * @return ResponseResult.
     */
    ResponseResult changePositionSide(boolean dual);

    /**
     * Change margin type (ISOLATED, CROSSED)
     * @param symbolName
     * @param marginType
     * @return
     */
    ResponseResult changeMarginType(String symbolName, String marginType);

    /**
     * add isolated position margin
     * @param symbolName
     * @param type
     * @param amount
     * @param positionSide SHORT, LONG, BOTH
     * @return
     */
    JSONObject addIsolatedPositionMargin(String symbolName, int type, String amount, PositionSide positionSide);

    /**
     *  get position margin history
     * @param symbolName
     * @param type
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    List<WalletDeltaLog> getPositionMarginHistory(String symbolName, int type, long startTime, long endTime, int limit);

    /**
     * Get if changed to HEDGE mode. (true == hedge mode, false == one-way mode)
     *
     * @return ResponseResult.
     */
    JSONObject getPositionSide();

    /**
     * Check an order's status.
     *
     * @return Order status.
     */
    FuturesOrder getOrder(String symbol, Long orderId, String origClientOrderId);

    /**
     * Get all open orders on a symbol. Careful when accessing this with no symbol.
     *
     * @return Open orders.
     */
    List<FuturesOrder> getOpenOrders(String symbol);

    /**
     * Get all account orders; active, canceled, or filled.
     *
     * @return All orders.
     */
    List<FuturesOrder> getAllOrders(String symbol, Long orderId, Long startTime, Long endTime, Integer limit);
  
    /**
     * Get account balances.
     *
     * @return Balances.
     */
    List<AccountBalance> getBalance();
  
    /**
     * Get current account information.
     *
     * @return Current account information.
     */
    AccountInformation getAccountInformation();
  
    /**
     * Change initial leverage.
     *
     * @return Leverage.
     */
    Leverage changeInitialLeverage(String symbol, Integer leverage);

    /**
     * Get position.
     *
     * @return Position.
     */
    List<PositionRisk> getPositionRisk();

    /**
     * Get trades for a specific account and symbol.
     *
     * @return Trades.
     */
    List<MyTrade> getAccountTrades(String symbol, Long startTime, Long endTime, Long fromId, Integer limit);

    /**
     * Get income history.
     *
     * @return Income history.
     */
    List<Income> getIncomeHistory(String symbol, IncomeType incomeType, Long startTime, Long endTime, Integer limit);

    /**
     * Start user data stream.
     *
     * @return listenKey.
     */
    String startUserDataStream();

    /**
     * Keep user data stream.
     *
     * @return null.
     */
    String keepUserDataStream(String listenKey);

    /**
     * Close user data stream.
     *
     * @return null.
     */
    String closeUserDataStream(String listenKey);

    /**
     * Open Interest Stat (MARKET DATA)
     *
     * @return Open Interest Stat.
     */
    List<OpenInterestStat> getOpenInterestStat(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit);

    /**
     * Top Trader Long/Short Ratio (Accounts) (MARKET DATA)
     *
     * @return Top Trader Long/Short Ratio (Accounts).
     */
    List<CommonLongShortRatio> getTopTraderAccountRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit);

    /**
     * Top Trader Long/Short Ratio (Positions) (MARKET DATA)
     *
     * @return Top Trader Long/Short Ratio (Positions).
     */
    List<CommonLongShortRatio> getTopTraderPositionRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit);

    /**
     * Long/Short Ratio (MARKET DATA)
     *
     * @return global Long/Short Ratio. 
     */
    List<CommonLongShortRatio> getGlobalAccountRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit);

    /**
     * Taker Long/Short Ratio (MARKET DATA)
     *
     * @return Taker Long/Short Ratio. 
     */
    List<TakerLongShortStat> getTakerLongShortRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit);

}