package com.univocity.trader.exchange.binance.futures.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.univocity.trader.exchange.binance.futures.RequestOptions;
import com.univocity.trader.exchange.binance.futures.exception.BinanceApiException;
import com.univocity.trader.exchange.binance.futures.impl.utils.JsonWrapperArray;
import com.univocity.trader.exchange.binance.futures.impl.utils.UrlParamsBuilder;
import com.univocity.trader.exchange.binance.futures.model.ResponseResult;

import com.univocity.trader.exchange.binance.futures.model.enums.*;
import com.univocity.trader.exchange.binance.futures.model.market.*;
import com.univocity.trader.exchange.binance.futures.model.trade.*;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;

class RestApiRequestImpl {

    private String apiKey;
    private String secretKey;
    private String serverUrl;

    RestApiRequestImpl(String apiKey, String secretKey, RequestOptions options) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.serverUrl = options.getUrl();
    }

    private Request createRequestByGet(String address, UrlParamsBuilder builder) {
        //Request request = createRequestByGet(serverUrl, address, builder);
        return createRequestByGet(serverUrl, address, builder);
    }

    private Request createRequestByGet(String url, String address, UrlParamsBuilder builder) {
        return createRequest(url, address, builder);
    }

    private Request createRequest(String url, String address, UrlParamsBuilder builder) {
        String requestUrl = url + address;
        System.out.print(requestUrl);
        if (builder != null) {
            if (builder.hasPostParam()) {
                return new Request.Builder().url(requestUrl).post(builder.buildPostBody())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
            } else {
                return new Request.Builder().url(requestUrl + builder.buildUrl())
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
            }
        } else {
            return new Request.Builder().url(requestUrl).addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        }
    }

    private Request createRequestWithSignature(String url, String address, UrlParamsBuilder builder) {
        if (builder == null) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Invoking] Builder is null when create request with Signature");
        }
        String requestUrl = url + address;
        new ApiSignature().createSignature(apiKey, secretKey, builder);
        if (builder.hasPostParam()) {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl).post(builder.buildPostBody())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("PUT")) {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl)
                    .put(builder.buildPostBody())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("DELETE")) {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl)
                    .delete()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .build();
        } else {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .build();
        }
    }

    private Request createRequestByPostWithSignature(String address, UrlParamsBuilder builder) {
        return createRequestWithSignature(serverUrl, address, builder.setMethod("POST"));
    }

    private Request createRequestByGetWithSignature(String address, UrlParamsBuilder builder) {
        return createRequestWithSignature(serverUrl, address, builder);
    }

    private Request createRequestByPutWithSignature(String address, UrlParamsBuilder builder) {
        return createRequestWithSignature(serverUrl, address, builder.setMethod("PUT"));
    }

    private Request createRequestByDeleteWithSignature(String address, UrlParamsBuilder builder) {
        return createRequestWithSignature(serverUrl, address, builder.setMethod("DELETE"));
    }

    private Request createRequestWithApikey(String url, String address, UrlParamsBuilder builder) {
        if (builder == null) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Invoking] Builder is null when create request with Signature");
        }
        String requestUrl = url + address;
        requestUrl += builder.buildUrl();
        if (builder.hasPostParam()) {
            return new Request.Builder().url(requestUrl)
                    .post(builder.buildPostBody())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("DELETE")) {
            return new Request.Builder().url(requestUrl)
                    .delete()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("PUT")) {
            return new Request.Builder().url(requestUrl)
                    .put(builder.buildPostBody())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else {
            return new Request.Builder().url(requestUrl)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        }
    }

    private Request createRequestByGetWithApikey(String address, UrlParamsBuilder builder) {
        return createRequestWithApikey(serverUrl, address, builder);
    }

    /**
     * 获取交易规则和交易对
     *
     * @return 交易对信息
     */
    RestApiRequest<ExchangeInformation> getExchangeInformation() {
        RestApiRequest<ExchangeInformation> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        //获取交易规则和交易对
        request.request = createRequestByGet("/fapi/v1/exchangeInfo", builder);

        request.jsonParser = (jsonWrapper -> {
            ExchangeInformation result = new ExchangeInformation();
            result.setTimezone(jsonWrapper.getString("timezone"));
            result.setServerTime(jsonWrapper.getLong("serverTime")); // 系统时间

            List<RateLimit> elementList = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("rateLimits");
            dataArray.forEach((item) -> {
                RateLimit element = new RateLimit();
                element.setRateLimitType(item.getString("rateLimitType"));// 按照访问权重来计算
                element.setInterval(item.getString("interval"));// 按照分钟计算
                element.setIntervalNum(item.getLong("intervalNum"));// 按照1分钟计算
                element.setLimit(item.getLong("limit"));// 上限次数
                elementList.add(element);
            });
            result.setRateLimits(elementList);

            List<ExchangeFilter> filterList = new LinkedList<>();
            JsonWrapperArray filterArray = jsonWrapper.getJsonArray("exchangeFilters");
            filterArray.forEach((item) -> {
                ExchangeFilter filter = new ExchangeFilter();
                filter.setFilterType(item.getString("filterType"));
                filter.setMaxNumOrders(item.getLong("maxNumOrders"));
                filter.setMaxNumAlgoOrders(item.getLong("maxNumAlgoOrders"));
                filterList.add(filter);
            });
            result.setExchangeFilters(filterList);

            List<ExchangeInfoEntry> symbolList = new LinkedList<>();
            JsonWrapperArray symbolArray = jsonWrapper.getJsonArray("symbols"); // 交易对信息
            symbolArray.forEach((item) -> {
                ExchangeInfoEntry symbol = new ExchangeInfoEntry();
                symbol.setSymbol(item.getString("symbol"));  // 交易对
                symbol.setStatus(item.getString("status"));// 交易对状态
                symbol.setMaintMarginPercent(item.getBigDecimal("maintMarginPercent"));// 请忽略
                symbol.setRequiredMarginPercent(item.getBigDecimal("requiredMarginPercent"));// 请忽略
                symbol.setBaseAsset(item.getString("baseAsset"));// 标的资产
                symbol.setQuoteAsset(item.getString("quoteAsset")); // 报价资产
                symbol.setPricePrecision(item.getLong("pricePrecision"));// 价格小数点位数
                symbol.setQuantityPrecision(item.getLong("quantityPrecision"));// 数量小数点位数
                symbol.setBaseAssetPrecision(item.getLong("baseAssetPrecision"));// 标的资产精度
                symbol.setQuotePrecision(item.getLong("quotePrecision"));// 报价资产精度
                symbol.setOrderTypes(item.getJsonArray("orderTypes").convert2StringList());// 订单类型
                symbol.setTimeInForce(item.getJsonArray("timeInForce").convert2StringList());// 有效方式
                List<List<Map<String, String>>> valList = new LinkedList<>();
                JsonWrapperArray valArray = item.getJsonArray("filters");
                valArray.forEach((val) -> {
                    valList.add(val.convert2DictList());
                    /*
                     "filterType": "PRICE_FILTER", // 价格限制
                    "maxPrice": "300", // 价格上限, 最大价格
                    "minPrice": "0.0001", // 价格下限, 最小价格
                    "tickSize": "0.0001" // 步进间隔
                     */
                });
                symbol.setFilters(valList);
                symbolList.add(symbol);
            });
            result.setSymbols(symbolList);

            return result;
        });
        return request;
    }

    /**
     * 深度信息
     *
     * @param symbol YES 交易对
     * @param limit  NO	默认 500; 可选值:[5, 10, 20, 50, 100, 500, 1000]
     * @return 深度信息
     */
    RestApiRequest<OrderBook> getOrderBook(String symbol, Integer limit) {
        RestApiRequest<OrderBook> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("limit", limit);
        //深度信息
        request.request = createRequestByGet("/fapi/v1/depth", builder);

        request.jsonParser = (jsonWrapper -> {
            OrderBook result = new OrderBook();
            result.setLastUpdateId(jsonWrapper.getLong("lastUpdateId"));

            List<OrderBookEntry> elementList = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("bids");
            dataArray.forEachAsArray((item) -> {
                OrderBookEntry element = new OrderBookEntry();
                element.setPrice(item.getBigDecimalAt(0));
                element.setQty(item.getBigDecimalAt(1));
                elementList.add(element);
            });
            result.setBids(elementList);

            List<OrderBookEntry> askList = new LinkedList<>();
            JsonWrapperArray askArray = jsonWrapper.getJsonArray("asks");
            askArray.forEachAsArray((item) -> {
                OrderBookEntry element = new OrderBookEntry();
                element.setPrice(item.getBigDecimalAt(0));
                element.setQty(item.getBigDecimalAt(1));
                askList.add(element);
            });
            result.setAsks(askList);

            return result;
        });
        return request;
    }

    /**
     * 近期成交
     *
     * @param symbol YES	交易对
     * @param limit  NO	默认:500，最大1000
     * @return 近期成交
     */
    RestApiRequest<List<Trade>> getRecentTrades(String symbol, Integer limit) {
        RestApiRequest<List<Trade>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("limit", limit);
        //近期成交
        request.request = createRequestByGet("/fapi/v1/trades", builder);

        request.jsonParser = (jsonWrapper -> {
            List<Trade> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                Trade element = new Trade();
                element.setId(item.getLong("id"));
                element.setPrice(item.getBigDecimal("price"));
                element.setQty(item.getBigDecimal("qty"));
                element.setQuoteQty(item.getBigDecimal("quoteQty"));
                element.setTime(item.getLong("time"));
                element.setIsBuyerMaker(item.getBoolean("isBuyerMaker"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 查询历史成交(需要有效的API-KEY)
     *
     * @param symbol YES	交易对
     * @param limit  NO	默认值:500 最大值:1000.
     * @param fromId NO	从哪一条成交id开始返回. 缺省返回最近的成交记录
     * @return List<Trade>
     */
    RestApiRequest<List<Trade>> getOldTrades(String symbol, Integer limit, Long fromId) {
        RestApiRequest<List<Trade>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("limit", limit)
                .putToUrl("fromId", fromId);
        //查询历史成交(MARKET_DATA)
        request.request = createRequestByGetWithApikey("/fapi/v1/historicalTrades", builder);

        request.jsonParser = (jsonWrapper -> {
            List<Trade> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                Trade element = new Trade();
                element.setId(item.getLong("id"));
                element.setPrice(item.getBigDecimal("price"));
                element.setQty(item.getBigDecimal("qty"));
                element.setQuoteQty(item.getBigDecimal("quoteQty"));
                element.setTime(item.getLong("time"));
                element.setIsBuyerMaker(item.getBoolean("isBuyerMaker"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 近期成交(归集)
     *
     * @param symbol    YES	交易对
     * @param fromId    NO	从包含fromID的成交开始返回结果
     * @param startTime NO	从该时刻之后的成交记录开始返回结果
     * @param endTime   NO	返回该时刻为止的成交记录
     * @param limit     NO	默认 500; 最大 1000.
     * @return 近期成交(归集)
     */
    RestApiRequest<List<AggregateTrade>> getAggregateTrades(String symbol, Long fromId,
                                                            Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<AggregateTrade>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("fromId", fromId)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //近期成交(归集)
        request.request = createRequestByGet("/fapi/v1/aggTrades", builder);

        request.jsonParser = (jsonWrapper -> {
            List<AggregateTrade> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                AggregateTrade element = new AggregateTrade();
                element.setId(item.getLong("a"));
                element.setPrice(item.getBigDecimal("p"));
                element.setQty(item.getBigDecimal("q"));
                element.setFirstId(item.getLong("f"));
                element.setLastId(item.getLong("l"));
                element.setTime(item.getLong("T"));
                element.setIsBuyerMaker(item.getBoolean("m"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * K线数据
     *
     * @param symbol    YES	交易对
     * @param interval  YES	时间间隔
     * @param startTime NO	起始时间
     * @param endTime   NO	结束时间
     * @param limit     NO	默认值:500 最大值:1500
     * @return K线数据
     */
    RestApiRequest<List<Candlestick>> getCandlestick(String symbol, CandlestickInterval interval, Long startTime,
                                                     Long endTime, Integer limit) {
        RestApiRequest<List<Candlestick>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("interval", interval)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //K线数据
        request.request = createRequestByGet("/fapi/v1/klines", builder);

        request.jsonParser = (jsonWrapper -> {
            List<Candlestick> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEachAsArray((item) -> {
                Candlestick element = new Candlestick();
                element.setOpenTime(item.getLongAt(0));
                element.setOpen(item.getStringAt(1));
                element.setHigh(item.getStringAt(2));
                element.setLow(item.getStringAt(3));
                element.setClose(item.getStringAt(4));
                element.setVolume(item.getStringAt(5));
                element.setCloseTime(item.getLongAt(6));
                element.setQuoteAssetVolume(item.getStringAt(7));
                //element.setNumTrades(item.getIntegerAt(8));
                element.setTakerBuyBaseAssetVolume(item.getStringAt(9));
                element.setTakerBuyQuoteAssetVolume(item.getStringAt(10));
                //element.setIgnore(item.getBigDecimalAt(11));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 最新标记价格和资金费率
     *
     * @param symbol NO	交易对
     * @return 采集各大交易所数据加权平均
     */
    RestApiRequest<List<MarkPrice>> getMarkPrice(String symbol) {
        RestApiRequest<List<MarkPrice>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol);
        //最新标记价格和资金费率
        request.request = createRequestByGet("/fapi/v1/premiumIndex", builder);

        request.jsonParser = (jsonWrapper -> {
            List<MarkPrice> result = new LinkedList<>();
            JsonWrapperArray dataArray = new JsonWrapperArray(new JSONArray());
            if (jsonWrapper.containKey("data")) {
                dataArray = jsonWrapper.getJsonArray("data");
            } else {
                dataArray.add(jsonWrapper.convert2JsonObject());
            }
            dataArray.forEach((item) -> {
                MarkPrice element = new MarkPrice();
                element.setSymbol(item.getString("symbol"));
                element.setMarkPrice(item.getBigDecimal("markPrice"));
                element.setLastFundingRate(item.getBigDecimal("lastFundingRate"));
                element.setNextFundingTime(item.getLong("nextFundingTime"));
                element.setTime(item.getLong("time"));
                result.add(element);
            });

            return result;

        });
        return request;
    }

    /**
     * 查询资金费率历史
     * 如果 startTime 和 endTime 都未发送, 返回最近 limit 条数据.
     * 如果 startTime 和 endTime 之间的数据量大于 limit, 返回 startTime + limit情况下的数据。
     *
     * @param symbol    YES	交易对
     * @param startTime NO	起始时间
     * @param endTime   NO	结束时间
     * @param limit     NO	默认值:100 最大值:1000
     * @return 资金费率历史
     */
    RestApiRequest<List<FundingRate>> getFundingRate(String symbol, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<FundingRate>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //查询资金费率历史
        request.request = createRequestByGet("/fapi/v1/fundingRate", builder);

        request.jsonParser = (jsonWrapper -> {
            List<FundingRate> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach(item -> {
                FundingRate element = new FundingRate();
                element.setSymbol(item.getString("symbol"));
                element.setFundingRate(item.getBigDecimal("fundingRate"));
                element.setFundingTime(item.getLong("fundingTime"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 24hr价格变动情况
     * 请注意，不携带symbol参数会返回全部交易对数据，不仅数据庞大，而且权重极高
     *
     * @param symbol 交易对
     * @return PriceChangeTicker
     */
    RestApiRequest<List<PriceChangeTicker>> get24hrTickerPriceChange(String symbol) {
        RestApiRequest<List<PriceChangeTicker>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol);
        //24hr价格变动情况
        request.request = createRequestByGet("/fapi/v1/ticker/24hr", builder);

        request.jsonParser = (jsonWrapper -> {
            List<PriceChangeTicker> result = new LinkedList<>();
            JsonWrapperArray dataArray = new JsonWrapperArray(new JSONArray());
            if (jsonWrapper.containKey("data")) {
                dataArray = jsonWrapper.getJsonArray("data");
            } else {
                dataArray.add(jsonWrapper.convert2JsonObject());
            }
            dataArray.forEach((item) -> {
                PriceChangeTicker element = new PriceChangeTicker();
                element.setSymbol(item.getString("symbol"));
                element.setPriceChange(item.getBigDecimal("priceChange"));
                element.setPriceChangePercent(item.getBigDecimal("priceChangePercent"));
                element.setWeightedAvgPrice(item.getBigDecimal("weightedAvgPrice"));
                element.setLastPrice(item.getBigDecimal("lastPrice"));
                element.setLastQty(item.getBigDecimal("lastQty"));
                element.setOpenPrice(item.getBigDecimal("openPrice"));
                element.setHighPrice(item.getBigDecimal("highPrice"));
                element.setLowPrice(item.getBigDecimal("lowPrice"));
                element.setVolume(item.getBigDecimal("volume"));
                element.setQuoteVolume(item.getBigDecimal("quoteVolume"));
                element.setOpenTime(item.getLong("openTime"));
                element.setCloseTime(item.getLong("closeTime"));
                element.setFirstId(item.getLong("firstId"));
                element.setLastId(item.getLong("lastId"));
                element.setCount(item.getLong("count"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 最新价格
     * 不发送交易对参数，则会返回所有交易对信息
     *
     * @param symbol NO	交易对
     * @return 返回最近价格
     */
    RestApiRequest<List<SymbolPrice>> getSymbolPriceTicker(String symbol) {
        RestApiRequest<List<SymbolPrice>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol);
        //最新价格
        request.request = createRequestByGet("/fapi/v1/ticker/price", builder);

        request.jsonParser = (jsonWrapper -> {
            List<SymbolPrice> result = new LinkedList<>();
            JsonWrapperArray dataArray = new JsonWrapperArray(new JSONArray());
            if (jsonWrapper.containKey("data")) {
                dataArray = jsonWrapper.getJsonArray("data");
            } else {
                dataArray.add(jsonWrapper.convert2JsonObject());
            }
            dataArray.forEach((item) -> {
                SymbolPrice element = new SymbolPrice();
                element.setSymbol(item.getString("symbol"));
                element.setPrice(item.getBigDecimal("price"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 当前最优挂单
     *
     * @param symbol NO 交易对
     * @return 返回当前最优的挂单(最高买单 ， 最低卖单)
     */
    RestApiRequest<List<SymbolOrderBook>> getSymbolOrderBookTicker(String symbol) {
        RestApiRequest<List<SymbolOrderBook>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol);
        //当前最优挂单
        request.request = createRequestByGet("/fapi/v1/ticker/bookTicker", builder);

        request.jsonParser = (jsonWrapper -> {
            List<SymbolOrderBook> result = new LinkedList<>();
            JsonWrapperArray dataArray = new JsonWrapperArray(new JSONArray());
            if (jsonWrapper.containKey("data")) {
                dataArray = jsonWrapper.getJsonArray("data");
            } else {
                dataArray.add(jsonWrapper.convert2JsonObject());
            }
            dataArray.forEach((item) -> {
                SymbolOrderBook element = new SymbolOrderBook();
                element.setSymbol(item.getString("symbol"));
                element.setBidPrice(item.getBigDecimal("bidPrice"));
                element.setBidQty(item.getBigDecimal("bidQty"));
                element.setAskPrice(item.getBigDecimal("askPrice"));
                element.setAskQty(item.getBigDecimal("askQty"));
                result.add(element);
            });

            return result;
        });
        return request;
    }

    /**
     * 获取市场强平订单
     * 如果不提供symbol,返回全市场强平订单。
     *
     * @param symbol    NO	交易对
     * @param startTime NO	起始时间
     * @param endTime   NO	结束时间,默认当前时间
     * @param limit     NO	从endTime倒推算起的数据条数，默认值:100 最大值:1000
     * @return 市场强平订单
     */
    RestApiRequest<List<LiquidationOrder>> getLiquidationOrders(String symbol, Long startTime, Long endTime,
                                                                Integer limit) {
        RestApiRequest<List<LiquidationOrder>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //获取市场强平订单
        request.request = createRequestByGetWithApikey("/fapi/v1/allForceOrders", builder);

        request.jsonParser = (jsonWrapper -> {
            List<LiquidationOrder> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");

            dataArray.forEach((item) -> {
                LiquidationOrder element = new LiquidationOrder();
                element.setSymbol(item.getString("symbol"));
                element.setPrice(item.getBigDecimal("price"));
                element.setOrigQty(item.getBigDecimal("origQty"));
                element.setExecutedQty(item.getBigDecimal("executedQty"));
                element.setAveragePrice(item.getBigDecimal("averagePrice"));
                element.setTimeInForce(item.getString("timeInForce"));
                element.setType(item.getString("symbol"));
                element.setSide(item.getString("side"));
                element.setTime(item.getLong("time"));
                result.add(element);
            });

            return result;
        });
        return request;
    }


    /**
     * 批量下单 (TRADE)
     * 其中batchOrders应以list of JSON格式填写订单参数
     *
     * @param batchOrders YES	订单列表，最多支持5个订单
     * @return 批量下单
     */
    RestApiRequest<List<Object>> postBatchOrders(String batchOrders) {
        RestApiRequest<List<Object>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("batchOrders", batchOrders);
        //批量下单 (TRADE)
        request.request = createRequestByPostWithSignature("/fapi/v1/batchOrders", builder);

        request.jsonParser = (jsonWrapper -> {
            JSONObject jsonObject = jsonWrapper.getJson();

            // success results
            List<Object> listResult = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jsonObject.get("data");
            jsonArray.forEach(obj -> {
                if (((JSONObject) obj).containsKey("code")) {
                    ResponseResult responseResult = new ResponseResult();
                    responseResult.setCode(((JSONObject) obj).getInteger("code"));
                    responseResult.setMsg(((JSONObject) obj).getString("msg"));
                    listResult.add(responseResult);
                } else {
                    FuturesOrder o = new FuturesOrder();
                    JSONObject jsonObj = (JSONObject) obj;
                    o.setClientOrderId(jsonObj.getString("clientOrderId"));
                    o.setCumQuote(jsonObj.getBigDecimal("cumQuote"));
                    o.setExecutedQty(jsonObj.getBigDecimal("executedQty"));
                    o.setOrderId(jsonObj.getLong("orderId"));
                    o.setOrigQty(jsonObj.getBigDecimal("origQty"));
                    o.setPrice(jsonObj.getBigDecimal("price"));
                    o.setReduceOnly(jsonObj.getBoolean("reduceOnly"));
                    //o.setSide(jsonObj.getString("side"));
                    o.setSide(OrderSide.valueOf(jsonObj.getString("side")));// 买卖方向
                    //o.setPositionSide(jsonObj.getString("positionSide"));
                    o.setPositionSide(PositionSide.valueOf(jsonObj.getString("positionSide")));// 持仓方向
                    o.setStatus(OrderStatus.lookup(jsonObj.getString("status")));
                    o.setStopPrice(jsonObj.getBigDecimal("stopPrice"));
                    o.setSymbol(jsonObj.getString("symbol"));
                    //o.setTimeInForce(jsonObj.getString("timeInForce"));
                    o.setTimeInForce(TimeInForce.valueOf(jsonObj.getString("timeInForce")));// 有效方法
                    //o.setType(jsonObj.getString("type"));
                    o.setType(OrderType.valueOf(jsonObj.getString("type")));// 订单类型
                    o.setUpdateTime(jsonObj.getLong("updateTime"));
                    //o.setWorkingType(jsonObj.getString("workingType"));
                    o.setWorkingType(WorkingType.valueOf(jsonObj.getString("workingType")));// 条件价格触发类型
                    listResult.add(o);
                }
            });
            return listResult;
        });
        return request;
    }

    /**
     * 下单 (TRADE)
     *
     * @param symbol           YES	交易对
     * @param side             YES	买卖方向 SELL, BUY
     * @param positionSide     NO	持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
     * @param orderType        YES	订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
     * @param timeInForce      NO	有效方法
     * @param quantity         NO	下单数量,使用closePosition不支持此参数。
     * @param price            NO	委托价格
     * @param reduceOnly       NO	true, false; 非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。
     * @param newClientOrderId NO	用户自定义的订单号，不可以重复出现在挂单中。如空缺系统会自动赋值
     * @param stopPrice        NO	触发价, 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
     * @param workingType      NO	stopPrice 触发类型: MARK_PRICE(标记价格), CONTRACT_PRICE(合约最新价). 默认 CONTRACT_PRICE
     * @param newOrderRespType NO	"ACK", "RESULT", 默认 "ACK"
     * @param closePosition    NO	true, false；触发后全部平仓，仅支持STOP_MARKET和TAKE_PROFIT_MARKET；不与quantity合用；自带只平仓效果，不与reduceOnly 合用
     * @param activationPrice  NO	追踪止损激活价格，仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
     * @param callbackRate     NO	追踪止损回调比例，可取值范围[0.1, 5],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
     * @param callbackRate     NO	追踪止损回调比例，可取值范围[0.1, 5],其中 1代表1% ,仅TRAILING_STOP_MARKET 需要此参数
     * @param priceProtect     NO	条件单触发保护："TRUE","FALSE", 默认"FALSE". 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
     * @param recvWindow       NO
     * @param timestamp        NO
     * @return 下单
     */
    RestApiRequest<FuturesOrder> postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType,
                                           TimeInForce timeInForce, BigDecimal quantity, BigDecimal price, /*Boolean reduceOnly,*/
                                           String newClientOrderId, BigDecimal stopPrice,
                                           WorkingType workingType, NewOrderRespType newOrderRespType) {
        RestApiRequest<FuturesOrder> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                        .putToUrl("symbol", symbol)
                        .putToUrl("side", side)
                        .putToUrl("positionSide", positionSide)
                        .putToUrl("type", orderType)
                        .putToUrl("timeInForce", timeInForce)
                        .putToUrl("quantity", quantity)
                        .putToUrl("price", price)
                        .putToUrl("newClientOrderId", newClientOrderId)
                        .putToUrl("stopPrice", stopPrice)
                        .putToUrl("workingType", workingType)
                        .putToUrl("newOrderRespType", newOrderRespType);
        //查询订单 (USER_DATA)
        request.request = createRequestByPostWithSignature("/fapi/v1/order", builder);

        request.jsonParser = (jsonWrapper -> {
            FuturesOrder result = new FuturesOrder();
            result.setClientOrderId(jsonWrapper.getString("clientOrderId")); // 用户自定义的订单号
            result.setCumQuote(jsonWrapper.getBigDecimal("cumQuote")); // 成交金额
            result.setExecutedQty(jsonWrapper.getBigDecimal("executedQty")); // 成交量
            result.setOrderId(jsonWrapper.getLong("orderId")); // 系统订单号
            result.setOrigQty(jsonWrapper.getBigDecimal("origQty")); // 原始委托数量
            result.setPrice(jsonWrapper.getBigDecimal("price")); // 委托价格
            result.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));// 仅减仓
            result.setSide(OrderSide.valueOf(jsonWrapper.getString("side")));// 买卖方向
            result.setPositionSide(PositionSide.valueOf(jsonWrapper.getString("positionSide")));// 持仓方向
            result.setStatus(OrderStatus.lookup(jsonWrapper.getString("status")));// 订单状态
            result.setStopPrice(jsonWrapper.getBigDecimal("stopPrice"));// 触发价，对`TRAILING_STOP_MARKET`无效
            result.setSymbol(jsonWrapper.getString("symbol")); //// 交易对
            result.setTimeInForce(TimeInForce.valueOf(jsonWrapper.getString("timeInForce")));// 有效方法
            result.setType(OrderType.valueOf(jsonWrapper.getString("type")));// 订单类型
            result.setUpdateTime(jsonWrapper.getLong("updateTime"));// 更新时间
            result.setWorkingType(WorkingType.valueOf(jsonWrapper.getString("workingType")));// 条件价格触发类型
            return result;
        });
        return request;
    }

    /**
     * 更改持仓模式(TRADE)
     *
     * @param dual YES	"true": 双向持仓模式；"false": 单向持仓模式
     * @return 持仓模式
     */
    RestApiRequest<ResponseResult> changePositionSide(boolean dual) {
        RestApiRequest<ResponseResult> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("dualSidePosition", String.valueOf(dual));
        //更改持仓模式(TRADE)
        request.request = createRequestByPostWithSignature("/fapi/v1/positionSide/dual", builder);

        request.jsonParser = (jsonWrapper -> {
            ResponseResult result = new ResponseResult();
            result.setCode(jsonWrapper.getInteger("code"));
            result.setMsg(jsonWrapper.getString("msg"));
            return result;
        });
        return request;
    }

    /**
     * 变换逐全仓模式 (TRADE)
     *
     * @param symbolName     YES	交易对
     * @param marginType YES	保证金模式 ISOLATED(逐仓), CROSSED(全仓
     * @return 逐全仓模式
     */
    RestApiRequest<ResponseResult> changeMarginType(String symbolName, String marginType) {
        RestApiRequest<ResponseResult> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbolName)
                .putToUrl("marginType", marginType);
        //变换逐全仓模式 (TRADE)
        request.request = createRequestByPostWithSignature("/fapi/v1/marginType", builder);

        request.jsonParser = (jsonWrapper -> {
            ResponseResult result = new ResponseResult();
            result.setCode(jsonWrapper.getInteger("code"));
            result.setMsg(jsonWrapper.getString("msg"));
            return result;
        });
        return request;
    }

    /**
     * 调整逐仓保证金 (TRADE)
     *
     * @param symbolName       YES	交易对
     * @param type         YES	调整方向 1: 增加逐仓保证金，2: 减少逐仓保证金
     * @param amount       YES	保证金资金
     * @param positionSide NO	持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
     * @return 调整逐仓保证金
     */
    RestApiRequest<JSONObject> addPositionMargin(String symbolName, int type, String amount, PositionSide positionSide) {
        RestApiRequest<JSONObject> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbolName)
                .putToUrl("amount", amount)
                .putToUrl("positionSide", positionSide.name())
                .putToUrl("type", type);
        //调整逐仓保证金 (TRADE)
        request.request = createRequestByPostWithSignature("/fapi/v1/positionMargin", builder);

        request.jsonParser = (jsonWrapper -> {
            JSONObject result = new JSONObject();
            result.put("code", jsonWrapper.getInteger("code"));
            result.put("msg", jsonWrapper.getString("msg"));
            result.put("amount", jsonWrapper.getDouble("amount"));
            result.put("type", jsonWrapper.getInteger("type"));
            //TODO 新增字段未补充
            return result;
        });
        return request;
    }

    /**
     * 逐仓保证金变动历史 (TRADE)
     *
     * @param symbolName    YES	交易对
     * @param type      YES	调整方向 1: 增加逐仓保证金，2: 减少逐仓保证金
     * @param startTime NO	起始时间
     * @param endTime   NO	结束时间
     * @param limit     NO	返回的结果集数量 默认值: 500
     * @return 保证金变动历史
     */
    RestApiRequest<List<WalletDeltaLog>> getPositionMarginHistory(String symbolName, int type, long startTime, long endTime, int limit) {
        RestApiRequest<List<WalletDeltaLog>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbolName)
                .putToUrl("type", type)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //逐仓保证金变动历史 (TRADE)
        request.request = createRequestByGet("/fapi/v1/positionMargin/history", builder);

        request.jsonParser = (jsonWrapper -> {
            List<WalletDeltaLog> logs = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                WalletDeltaLog log = new WalletDeltaLog();
                log.setSymbol(item.getString("symbol"));
                log.setAmount(item.getString("amount"));
                log.setAsset(item.getString("asset"));
                log.setTime(item.getLong("time"));
                log.setPositionSide(item.getString("positionSide"));
                log.setType(item.getInteger("type"));
                logs.add(log);
            });
            return logs;
        });
        return request;
    }

    /**
     * 查询持仓模式(需要有效的API-KEY)
     *
     * @return 持仓模式
     */
    RestApiRequest<JSONObject> getPositionSide() {
        RestApiRequest<JSONObject> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        //更改持仓模式(TRADE)
        request.request = createRequestByGetWithSignature("/fapi/v1/positionSide/dual", builder);

        request.jsonParser = (jsonWrapper -> {
            JSONObject result = new JSONObject();
            result.put("dualSidePosition", jsonWrapper.getBoolean("dualSidePosition"));
            return result;
        });
        return request;
    }

    /**
     * 撤销订单 (TRADE)
     *
     * @param symbol            YES	交易对
     * @param orderId           NO	系统订单号
     * @param origClientOrderId NO	用户自定义的订单号
     * @return 撤销订单
     */
    RestApiRequest<FuturesOrder> cancelOrder(String symbol, Long orderId, String origClientOrderId) {
        RestApiRequest<FuturesOrder> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("orderId", orderId)
                .putToUrl("origClientOrderId", origClientOrderId);
        //下单 (TRADE)
        request.request = createRequestByDeleteWithSignature("/fapi/v1/order", builder);

        request.jsonParser = (jsonWrapper -> {
            FuturesOrder result = new FuturesOrder();
            result.setClientOrderId(jsonWrapper.getString("clientOrderId"));
            result.setCumQuote(jsonWrapper.getBigDecimal("cumQuote"));
            result.setExecutedQty(jsonWrapper.getBigDecimal("executedQty"));
            result.setOrderId(jsonWrapper.getLong("orderId"));
            result.setOrigQty(jsonWrapper.getBigDecimal("origQty"));
            result.setPrice(jsonWrapper.getBigDecimal("price"));
            result.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
            result.setSide(OrderSide.valueOf(jsonWrapper.getString("side")));// 买卖方向
            result.setPositionSide(PositionSide.valueOf(jsonWrapper.getString("positionSide")));// 持仓方向
            result.setStatus(OrderStatus.lookup(jsonWrapper.getString("status")));
            result.setStopPrice(jsonWrapper.getBigDecimal("stopPrice"));
            result.setSymbol(jsonWrapper.getString("symbol"));
            result.setTimeInForce(TimeInForce.valueOf(jsonWrapper.getString("timeInForce")));// 有效方法
            result.setType(OrderType.valueOf(jsonWrapper.getString("type")));// 订单类型
            result.setUpdateTime(jsonWrapper.getLong("updateTime"));
            result.setWorkingType(WorkingType.valueOf(jsonWrapper.getString("workingType")));// 条件价格触发类型
            return result;
        });
        return request;
    }

    /**
     * 撤销全部订单 (TRADE)
     * orderId 与 origClientOrderId 必须至少发送一个
     *
     * @param symbol YES	交易对
     * @return 撤销全部订单
     */
    RestApiRequest<ResponseResult> cancelAllOpenOrder(String symbol) {
        RestApiRequest<ResponseResult> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol);
        //撤销全部订单 (TRADE)
        request.request = createRequestByDeleteWithSignature("/fapi/v1/allOpenOrders", builder);

        request.jsonParser = (jsonWrapper -> {
            ResponseResult responseResult = new ResponseResult();
            responseResult.setCode(jsonWrapper.getInteger("code"));
            responseResult.setMsg(jsonWrapper.getString("msg"));
            return responseResult;
        });
        return request;
    }

    /**
     * 批量撤销订单 (TRADE)
     * orderIdList 与 origClientOrderIdList 必须至少发送一个，不可同时发送
     *
     * @param symbol                YES	交易对
     * @param orderIdList           NO	系统订单号, 最多支持10个订单
     *                              比如[1234567,2345678]
     * @param origClientOrderIdList NO	用户自定义的订单号, 最多支持10个订单
     *                              比如["my_id_1","my_id_2"] 需要encode双引号。逗号后面没有空格。
     * @return 批量撤销订单
     */
    RestApiRequest<List<Object>> batchCancelOrders(String symbol, String orderIdList, String origClientOrderIdList) {
        RestApiRequest<List<Object>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        builder.putToUrl("symbol", symbol);
        if (StringUtils.isNotBlank(orderIdList)) {
            builder.putToUrl("orderIdList", orderIdList);
        } else {
            builder.putToUrl("origClientOrderIdList", origClientOrderIdList);
        }
        //批量撤销订单 (TRADE)
        request.request = createRequestByDeleteWithSignature("/fapi/v1/batchOrders", builder);

        request.jsonParser = (jsonWrapper -> {
            JSONObject jsonObject = jsonWrapper.getJson();

            // success results
            List<Object> listResult = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jsonObject.get("data");
            jsonArray.forEach(obj -> {
                if (((JSONObject)obj).containsKey("code")) {
                    ResponseResult responseResult = new ResponseResult();
                    responseResult.setCode(((JSONObject)obj).getInteger("code"));
                    responseResult.setMsg(((JSONObject)obj).getString("msg"));
                    listResult.add(responseResult);
                } else {
                    FuturesOrder o = new FuturesOrder();
                    JSONObject jsonObj = (JSONObject) obj;
                    o.setClientOrderId(jsonObj.getString("clientOrderId"));
                    o.setCumQuote(jsonObj.getBigDecimal("cumQuote"));
                    o.setExecutedQty(jsonObj.getBigDecimal("executedQty"));
                    o.setOrderId(jsonObj.getLong("orderId"));
                    o.setOrigQty(jsonObj.getBigDecimal("origQty"));
                    o.setPrice(jsonObj.getBigDecimal("price"));
                    o.setReduceOnly(jsonObj.getBoolean("reduceOnly"));
                    o.setSide(OrderSide.valueOf(jsonObj.getString("side")));// 买卖方向
                    o.setPositionSide(PositionSide.valueOf(jsonObj.getString("positionSide")));// 持仓方向
                    o.setStatus(OrderStatus.lookup(jsonObj.getString("status")));
                    o.setStopPrice(jsonObj.getBigDecimal("stopPrice"));
                    o.setSymbol(jsonObj.getString("symbol"));
                    o.setTimeInForce(TimeInForce.valueOf(jsonObj.getString("timeInForce")));// 有效方法
                    o.setType(OrderType.valueOf(jsonObj.getString("type")));// 订单类型
                    o.setUpdateTime(jsonObj.getLong("updateTime"));
                    o.setWorkingType(WorkingType.valueOf(jsonObj.getString("workingType")));// 条件价格触发类型
                    listResult.add(o);
                }
            });
            return listResult;
        });
        return request;
    }

    /**
     * 查询订单 (需要有效的API-KEY)
     *
     * @param symbol            YES	交易对
     * @param orderId           NO	系统订单号
     * @param origClientOrderId NO	用户自定义的订单号
     * @return 订单
     */
    RestApiRequest<FuturesOrder> getOrder(String symbol, Long orderId, String origClientOrderId) {
        RestApiRequest<FuturesOrder> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("orderId", orderId)
                .putToUrl("origClientOrderId", origClientOrderId);
        //查询订单 (USER_DATA)
        request.request = createRequestByGetWithSignature("/fapi/v1/order", builder);

        request.jsonParser = (jsonWrapper -> {
            FuturesOrder result = new FuturesOrder();
            result.setClientOrderId(jsonWrapper.getString("clientOrderId"));
            result.setCumQuote(jsonWrapper.getBigDecimal("cumQuote"));
            result.setExecutedQty(jsonWrapper.getBigDecimal("executedQty"));
            result.setOrderId(jsonWrapper.getLong("orderId"));
            result.setOrigQty(jsonWrapper.getBigDecimal("origQty"));
            result.setPrice(jsonWrapper.getBigDecimal("price"));
            result.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
            result.setSide(OrderSide.valueOf(jsonWrapper.getString("side")));// 买卖方向
            result.setPositionSide(PositionSide.valueOf(jsonWrapper.getString("positionSide")));// 持仓方向
            result.setStatus(OrderStatus.lookup(jsonWrapper.getString("status")));
            result.setStopPrice(jsonWrapper.getBigDecimal("stopPrice"));
            result.setSymbol(jsonWrapper.getString("symbol"));
            result.setTimeInForce(TimeInForce.valueOf(jsonWrapper.getString("timeInForce")));// 有效方法
            result.setType(OrderType.valueOf(jsonWrapper.getString("type")));// 订单类型
            result.setUpdateTime(jsonWrapper.getLong("updateTime"));
            result.setWorkingType(WorkingType.valueOf(jsonWrapper.getString("workingType")));// 条件价格触发类型
            return result;
        });
        return request;
    }

    /**
     * 查看当前全部挂单 (需要有效的API-KEY)
     *
     * @param symbol NO	交易对
     * @return 全部挂单
     */
    RestApiRequest<List<FuturesOrder>> getOpenOrders(String symbol) {
        RestApiRequest<List<FuturesOrder>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol);
        //查看当前全部挂单 (USER_DATA)
        request.request = createRequestByGetWithSignature("/fapi/v1/openOrders", builder);

        request.jsonParser = (jsonWrapper -> {
            List<FuturesOrder> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                FuturesOrder element = new FuturesOrder();
                element.setClientOrderId(item.getString("clientOrderId"));
                element.setCumQuote(item.getBigDecimal("cumQuote"));
                element.setExecutedQty(item.getBigDecimal("executedQty"));
                element.setOrderId(item.getLong("orderId"));
                element.setOrigQty(item.getBigDecimal("origQty"));
                element.setPrice(item.getBigDecimal("price"));
                element.setReduceOnly(item.getBoolean("reduceOnly"));
                element.setSide(OrderSide.valueOf(item.getString("side")));// 买卖方向
                element.setPositionSide(PositionSide.valueOf(item.getString("positionSide")));// 持仓方向
                element.setStatus(OrderStatus.lookup(item.getString("status")));
                element.setStopPrice(item.getBigDecimal("stopPrice"));
                element.setSymbol(item.getString("symbol"));
                element.setTimeInForce(TimeInForce.valueOf(item.getString("timeInForce")));// 有效方法
                element.setType(OrderType.valueOf(item.getString("type")));// 订单类型
                element.setUpdateTime(item.getLong("updateTime"));
                element.setWorkingType(WorkingType.valueOf(item.getString("workingType")));// 条件价格触发类型
                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 查询所有订单(包括历史订单) (需要有效的API-KEY)
     *
     * @param symbol    YES 交易对
     * @param orderId   NO 只返回此orderID及之后的订单，缺省返回最近的订单
     * @param startTime NO 起始时间
     * @param endTime   NO 结束时间
     * @param limit     NO	default 30, max 500
     * @return 所有订单
     */
    RestApiRequest<List<FuturesOrder>> getAllOrders(String symbol, Long orderId, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<FuturesOrder>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("orderId", orderId)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //查询所有订单(包括历史订单) (USER_DAT
        request.request = createRequestByGetWithSignature("/fapi/v1/allOrders", builder);

        request.jsonParser = (jsonWrapper -> {
            List<FuturesOrder> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                FuturesOrder element = new FuturesOrder();
                element.setClientOrderId(item.getString("clientOrderId"));
                element.setCumQuote(item.getBigDecimal("cumQuote"));
                element.setExecutedQty(item.getBigDecimal("executedQty"));
                element.setOrderId(item.getLong("orderId"));
                element.setOrigQty(item.getBigDecimal("origQty"));
                element.setPrice(item.getBigDecimal("price"));
                element.setReduceOnly(item.getBoolean("reduceOnly"));
                element.setSide(OrderSide.valueOf(item.getString("side")));// 买卖方向
                element.setPositionSide(PositionSide.valueOf(item.getString("positionSide")));// 持仓方向
                element.setStatus(OrderStatus.lookup(item.getString("status")));
                element.setStopPrice(item.getBigDecimal("stopPrice"));
                element.setSymbol(item.getString("symbol"));
                element.setTimeInForce(TimeInForce.valueOf(item.getString("timeInForce")));// 有效方法
                element.setType(OrderType.valueOf(item.getString("type")));// 订单类型
                element.setUpdateTime(item.getLong("updateTime"));
                element.setWorkingType(WorkingType.valueOf(item.getString("workingType")));// 条件价格触发类型
                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 账户余额V2 (需要有效的API-KEY)
     *
     * @return 账户余额V2 (需要有效的API-KEY)
     */
    RestApiRequest<List<AccountBalance>> getBalance() {
        RestApiRequest<List<AccountBalance>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        //request.request = createRequestByGetWithSignature("/fapi/v1/balance", builder);
        request.request = createRequestByGetWithSignature("/fapi/v2/balance", builder);

        request.jsonParser = (jsonWrapper -> {
            List<AccountBalance> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                AccountBalance element = new AccountBalance();
                element.setAccountAlias(item.getString("accountAlias"));
                element.setAsset(item.getString("asset"));
                element.setBalance(item.getBigDecimal("balance"));
                //element.setWithdrawAvailable(item.getBigDecimal("withdrawAvailable"));
                element.setAvailableBalance(item.getBigDecimal("availableBalance"));
                element.setMaxWithdrawAmount(item.getBigDecimal("maxWithdrawAmount"));
                element.setCrossWalletBalance(item.getBigDecimal("crossWalletBalance"));
                element.setCrossUnPnl(item.getBigDecimal("crossUnPnl"));
                result.add(element);
            });
            return result;
        });
        return request;
    }

    RestApiRequest<AccountInformation> getAccountInformation() {
        RestApiRequest<AccountInformation> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        //账户信息V2 (USER_DATA)
        request.request = createRequestByGetWithSignature("/fapi/v1/account", builder);

        request.jsonParser = (jsonWrapper -> {
            AccountInformation result = new AccountInformation();
            result.setCanDeposit(jsonWrapper.getBoolean("canDeposit"));
            result.setCanTrade(jsonWrapper.getBoolean("canTrade"));
            result.setCanWithdraw(jsonWrapper.getBoolean("canWithdraw"));
            result.setFeeTier(jsonWrapper.getBigDecimal("feeTier"));
            result.setMaxWithdrawAmount(jsonWrapper.getBigDecimal("maxWithdrawAmount"));
            result.setTotalInitialMargin(jsonWrapper.getBigDecimal("totalInitialMargin"));
            result.setTotalMaintMargin(jsonWrapper.getBigDecimal("totalMaintMargin"));
            result.setTotalMarginBalance(jsonWrapper.getBigDecimal("totalMarginBalance"));
            result.setTotalOpenOrderInitialMargin(jsonWrapper.getBigDecimal("totalOpenOrderInitialMargin"));
            result.setTotalPositionInitialMargin(jsonWrapper.getBigDecimal("totalPositionInitialMargin"));
            result.setTotalUnrealizedProfit(jsonWrapper.getBigDecimal("totalUnrealizedProfit"));
            result.setTotalWalletBalance(jsonWrapper.getBigDecimal("totalWalletBalance"));
            result.setUpdateTime(jsonWrapper.getLong("updateTime"));

            List<Asset> assetList = new LinkedList<>();
            JsonWrapperArray assetArray = jsonWrapper.getJsonArray("assets");
            assetArray.forEach((item) -> {
                Asset element = new Asset();
                element.setAsset(item.getString("asset"));
                element.setInitialMargin(item.getBigDecimal("initialMargin"));
                element.setMaintMargin(item.getBigDecimal("maintMargin"));
                element.setMarginBalance(item.getBigDecimal("marginBalance"));
                element.setMaxWithdrawAmount(item.getBigDecimal("maxWithdrawAmount"));
                element.setOpenOrderInitialMargin(item.getBigDecimal("openOrderInitialMargin"));
                element.setPositionInitialMargin(item.getBigDecimal("positionInitialMargin"));
                element.setUnrealizedProfit(item.getBigDecimal("unrealizedProfit"));
                assetList.add(element);
            });
            result.setAssets(assetList);

            List<Position> positionList = new LinkedList<>();
            JsonWrapperArray positionArray = jsonWrapper.getJsonArray("positions");
            positionArray.forEach((item) -> {
                Position element = new Position();
                element.setIsolated(item.getBoolean("isolated"));
                element.setLeverage(item.getBigDecimal("leverage"));
                element.setInitialMargin(item.getBigDecimal("initialMargin"));
                element.setMaintMargin(item.getBigDecimal("maintMargin"));
                element.setOpenOrderInitialMargin(item.getBigDecimal("openOrderInitialMargin"));
                element.setPositionInitialMargin(item.getBigDecimal("positionInitialMargin"));
                element.setSymbol(item.getString("symbol"));
                element.setUnrealizedProfit(item.getBigDecimal("unrealizedProfit"));
                element.setEntryPrice(item.getString("entryPrice"));
                element.setMaxNotional(item.getString("maxNotional"));
                element.setPositionSide(item.getString("positionSide"));
                positionList.add(element);
            });
            result.setPositions(positionList);
            return result;
        });
        return request;
    }

    /**
     * 调整开仓杠杆 (TRADE)
     *
     * @param symbol   YES	交易对
     * @param leverage YES	目标杠杆倍数：1 到 125 整数
     * @return 开仓杠杆
     */
    RestApiRequest<Leverage> changeInitialLeverage(String symbol, Integer leverage) {
        RestApiRequest<Leverage> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("leverage", leverage);
        //调整开仓杠杆 (TRADE)
        request.request = createRequestByPostWithSignature("/fapi/v1/leverage", builder);

        request.jsonParser = (jsonWrapper -> {
            Leverage result = new Leverage();
            result.setLeverage(jsonWrapper.getBigDecimal("leverage"));
            if(jsonWrapper.getString("maxNotionalValue").equals("INF")) {
                result.setMaxNotionalValue(Double.POSITIVE_INFINITY);
            } else {
                result.setMaxNotionalValue(jsonWrapper.getDouble("maxNotionalValue"));
            }
            result.setSymbol(jsonWrapper.getString("symbol"));
            return result;
        });
        return request;
    }

    /**
     * 用户持仓风险
     *
     * @return 用户持仓风险
     */
    RestApiRequest<List<PositionRisk>> getPositionRisk() {
        RestApiRequest<List<PositionRisk>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        request.request = createRequestByGetWithSignature("/fapi/v2/positionRisk", builder);

        request.jsonParser = (jsonWrapper -> {
            List<PositionRisk> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                PositionRisk element = new PositionRisk();
/*

                "entryPrice": "0.00000", // 开仓均价
                "marginType": "isolated", // 逐仓模式或全仓模式
                "isAutoAddMargin": "false",
                "isolatedMargin": "0.00000000", // 逐仓保证金
                "leverage": "10", // 当前杠杆倍数
                "liquidationPrice": "0", // 参考强平价格
                "markPrice": "6679.50671178",   // 当前标记价格
                "maxNotionalValue": "20000000", // 当前杠杆倍数允许的名义价值上限
                "positionAmt": "0.000", // 头寸数量，符号代表多空方向, 正数为多，负数为空
                "symbol": "BTCUSDT", // 交易对
                "unRealizedProfit": "0.00000000", // 持仓未实现盈亏
                "positionSide": "BOTH", // 持仓方向
*/
                element.setEntryPrice(item.getBigDecimal("entryPrice")); // 开仓均价
                element.setMarginType(item.getString("marginType")); // 逐仓模式或全仓模式
                element.setAutoAddMargin(item.getBoolean("isAutoAddMargin"));
                element.setIsolatedMargin(item.getBigDecimal("isolatedMargin")); // 逐仓保证金
                element.setLeverage(item.getInteger("leverage"));   // 当前杠杆倍数
                element.setLiquidationPrice(item.getBigDecimal("liquidationPrice"));// 参考强平价格
                element.setMarkPrice(item.getBigDecimal("markPrice"));// 当前标记价格
                element.setMaxNotionalValue(item.getLong("maxNotionalValue"));// 当前杠杆倍数允许的名义价值上限
                element.setPositionAmt(item.getBigDecimal("positionAmt"));// 当前杠杆倍数允许的名义价值上限
                element.setSymbol(item.getString("symbol")); // 交易对
                element.setUnRealizedProfit(item.getBigDecimal("unRealizedProfit"));
                element.setPositionSide(PositionSide.valueOf(item.getString("positionSide"))); // 持仓方向
                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 账户成交历史 (需要有效的API-KEY)
     *
     * @param symbol    YES 交易对
     * @param startTime NO
     * @param endTime   NO
     * @param fromId    NO	返回该fromId及之后的成交，缺省返回最近的成交
     * @param limit     NO	default 30, max 500
     * @return 账户成交历史
     */
    RestApiRequest<List<MyTrade>> getAccountTrades(String symbol, Long startTime, Long endTime, 
            Long fromId, Integer limit) {
        RestApiRequest<List<MyTrade>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("fromId", fromId)
                .putToUrl("limit", limit);
        //账户成交历史 (USER_DATA)
        request.request = createRequestByGetWithSignature("/fapi/v1/userTrades", builder);

        request.jsonParser = (jsonWrapper -> {
            List<MyTrade> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                MyTrade element = new MyTrade();
                element.setIsBuyer(item.getBoolean("buyer"));
                element.setCommission(item.getBigDecimal("commission"));
                element.setCommissionAsset(item.getString("commissionAsset"));
                element.setCounterPartyId(item.getLongOrDefault("counterPartyId", 0));
                element.setOrderId(item.getLong("orderId"));
                element.setIsMaker(item.getBoolean("maker"));
                element.setOrderId(item.getLong("orderId"));
                element.setPrice(item.getBigDecimal("price"));
                element.setQty(item.getBigDecimal("qty"));
                element.setQuoteQty(item.getBigDecimal("quoteQty"));
                element.setRealizedPnl(item.getBigDecimal("realizedPnl"));
                element.setSide(item.getString("side"));
                element.setPositionSide(item.getString("positionSide"));
                element.setSymbol(item.getString("symbol"));
                element.setTime(item.getLong("time"));
                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 获取账户损益资金流水(需要有效的API-KEY)
     * 如果incomeType没有发送，返回所有类型账户损益资金流水。
     *
     * @param symbol     YES 交易对
     * @param incomeType YES	资金流类型
     * @param startTime  NO
     * @param endTime    NO
     * @param limit      NO	default 30, max 500
     * @return 损益资金流水
     */
    RestApiRequest<List<Income>> getIncomeHistory(String symbol, IncomeType incomeType, Long startTime, Long endTime, 
            Integer limit) {
        RestApiRequest<List<Income>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("incomeType", incomeType)
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        //获取账户损益资金流水(USER_DATA)
        request.request = createRequestByGetWithSignature("/fapi/v1/income", builder);

        request.jsonParser = (jsonWrapper -> {
            List<Income> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                Income element = new Income();
                element.setSymbol(item.getString("symbol"));
                element.setIncomeType(item.getString("incomeType"));
                element.setIncome(item.getBigDecimal("income"));
                element.setAsset(item.getString("asset"));
                element.setTime(item.getLong("time"));
                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 生成listenKey (需要有效的API-KEY)
     *
     * @return 生成
     */
    RestApiRequest<String> startUserDataStream() {
        RestApiRequest<String> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();

        //生成listenKey (USER_STREAM)
        request.request = createRequestByPostWithSignature("/fapi/v1/listenKey", builder);

        request.jsonParser = (jsonWrapper -> {
            String result = jsonWrapper.getString("listenKey");
            return result;
        });
        return request;
    }

    /**
     * 延长listenKey有效期 (需要有效的API-KEY)
     *
     * @param listenKey listenKey
     * @return 有效期延长至本次调用后60分钟
     */
    RestApiRequest<String> keepUserDataStream(String listenKey) {
        RestApiRequest<String> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("listenKey", listenKey);

        //延长listenKey有效期 (USER_STREAM)
        request.request = createRequestByPutWithSignature("/fapi/v1/listenKey", builder);

        request.jsonParser = (jsonWrapper -> {
            String result = "Ok";
            return result;
        });
        return request;
    }

    /**
     * 关闭listenKey (需要有效的API-KEY)
     *
     * @param listenKey listenKey
     * @return 关闭某账户数据流
     */
    RestApiRequest<String> closeUserDataStream(String listenKey) {
        RestApiRequest<String> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("listenKey", listenKey);

        //关闭listenKey (USER_STREAM)
        request.request = createRequestByDeleteWithSignature("/fapi/v1/listenKey", builder);

        request.jsonParser = (jsonWrapper -> {
            String result = "Ok";
            return result;
        });
        return request;
    }

    /**
     * 合约持仓量
     *
     * @param symbol    YES 交易对
     * @param period    YES	"5m","15m","30m","1h","2h","4h","6h","12h","1d"
     * @param startTime NO
     * @param endTime   NO
     * @param limit     NO	default 30, max 500
     * @return 合约持仓量
     */
    RestApiRequest<List<OpenInterestStat>> getOpenInterestStat(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<OpenInterestStat>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("period", period.getCode())
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);
        
        //合约持仓量
//        request.request = createRequestByGetWithSignature("/gateway-api//v1/public/future/data/openInterestHist", builder);
        request.request = createRequestByGetWithSignature("/futures/data/openInterestHist", builder);

        request.jsonParser = (jsonWrapper -> {
            List<OpenInterestStat> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                OpenInterestStat element = new OpenInterestStat();
                element.setSymbol(item.getString("symbol"));
                element.setSumOpenInterest(item.getBigDecimal("sumOpenInterest"));
                element.setSumOpenInterestValue(item.getBigDecimal("sumOpenInterestValue"));
                element.setTimestamp(item.getLong("timestamp"));

                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 合约持仓量
     *
     * @param symbol    YES 交易对
     * @param period    YES	"5m","15m","30m","1h","2h","4h","6h","12h","1d"
     * @param startTime NO
     * @param endTime   NO
     * @param limit     NO	default 30, max 500
     * @return 合约持仓量
     */
    RestApiRequest<List<CommonLongShortRatio>> getTopTraderAccountRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<CommonLongShortRatio>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("period", period.getCode())
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);

        //大户账户数多空比
//        request.request = createRequestByGetWithSignature("/gateway-api//v1/public/future/data/topLongShortAccountRatio", builder);
        request.request = createRequestByGetWithSignature("/futures/data/topLongShortAccountRatio", builder);

        request.jsonParser = (jsonWrapper -> {
            List<CommonLongShortRatio> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                CommonLongShortRatio element = new CommonLongShortRatio();
                element.setSymbol(item.getString("symbol"));
                element.setLongAccount(item.getBigDecimal("longAccount"));
                element.setLongShortRatio(item.getBigDecimal("longShortRatio"));
                element.setShortAccount(item.getBigDecimal("shortAccount"));
                element.setTimestamp(item.getLong("timestamp"));

                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 合约持仓量
     *
     * @param symbol    YES 交易对
     * @param period    YES	"5m","15m","30m","1h","2h","4h","6h","12h","1d"
     * @param startTime NO
     * @param endTime   NO
     * @param limit     NO	default 30, max 500
     * @return 合约持仓量
     */
    RestApiRequest<List<CommonLongShortRatio>> getTopTraderPositionRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<CommonLongShortRatio>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("period", period.getCode())
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);


        //大户持仓量多空比
//        request.request = createRequestByGetWithSignature("/gateway-api//v1/public/future/data/topLongShortPositionRatio", builder);
        request.request = createRequestByGetWithSignature("/futures/data/topLongShortPositionRatio", builder);

        request.jsonParser = (jsonWrapper -> {
            List<CommonLongShortRatio> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                CommonLongShortRatio element = new CommonLongShortRatio();
                element.setSymbol(item.getString("symbol"));
                element.setLongAccount(item.getBigDecimal("longAccount"));
                element.setLongShortRatio(item.getBigDecimal("longShortRatio"));
                element.setShortAccount(item.getBigDecimal("shortAccount"));
                element.setTimestamp(item.getLong("timestamp"));

                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 多空持仓人数比
     *
     * @param symbol    YES 交易对
     * @param period    YES	"5m","15m","30m","1h","2h","4h","6h","12h","1d"
     * @param startTime NO
     * @param endTime   NO
     * @param limit     NO	default 30, max 500
     * @return 多空持仓人数比
     */
    RestApiRequest<List<CommonLongShortRatio>> getGlobalAccountRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<CommonLongShortRatio>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("period", period.getCode())
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);


        //多空持仓人数比
//        request.request = createRequestByGetWithSignature("/gateway-api//v1/public/future/data/globalLongShortAccountRatio", builder);
        request.request = createRequestByGetWithSignature("/futures/data/globalLongShortAccountRatio", builder);

        request.jsonParser = (jsonWrapper -> {
            List<CommonLongShortRatio> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                CommonLongShortRatio element = new CommonLongShortRatio();
                element.setSymbol(item.getString("symbol"));
                element.setLongAccount(item.getBigDecimal("longAccount"));
                element.setLongShortRatio(item.getBigDecimal("longShortRatio"));
                element.setShortAccount(item.getBigDecimal("shortAccount"));
                element.setTimestamp(item.getLong("timestamp"));

                result.add(element);
            });
            return result;
        });
        return request;
    }

    /**
     * 合约主动买卖量
     *
     * @param symbol    YES 交易对
     * @param period    YES	"5m","15m","30m","1h","2h","4h","6h","12h","1d"
     * @param startTime NO
     * @param endTime   NO
     * @param limit     NO	default 30, max 500
     * @return 合约主动买卖量
     */
    RestApiRequest<List<TakerLongShortStat>> getTakerLongShortRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        RestApiRequest<List<TakerLongShortStat>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("period", period.getCode())
                .putToUrl("startTime", startTime)
                .putToUrl("endTime", endTime)
                .putToUrl("limit", limit);

        //合约主动买卖量
//        request.request = createRequestByGetWithSignature("/gateway-api//v1/public/future/data/globalLongShortAccountRatio", builder);
        request.request = createRequestByGetWithSignature("/futures/data/takerlongshortRatio", builder);

        request.jsonParser = (jsonWrapper -> {
            List<TakerLongShortStat> result = new LinkedList<>();
            JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");
            dataArray.forEach((item) -> {
                TakerLongShortStat element = new TakerLongShortStat();
                element.setBuySellRatio(item.getBigDecimal("buySellRatio"));
                element.setSellVol(item.getBigDecimal("sellVol"));
                element.setBuyVol(item.getBigDecimal("buyVol"));
                element.setTimestamp(item.getLong("timestamp"));

                result.add(element);
            });
            return result;
        });
        return request;
    }

}
