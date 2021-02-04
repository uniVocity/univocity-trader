package com.univocity.trader.exchange.binance.futures;

import com.univocity.trader.Exchange;
import com.univocity.trader.candles.PreciseCandle;
import com.univocity.trader.candles.SymbolInformation;
import com.univocity.trader.candles.TickConsumer;
import com.univocity.trader.exchange.binance.futures.impl.BinanceApiInternalFactory;
import com.univocity.trader.exchange.binance.futures.impl.HttpUtils;
import com.univocity.trader.exchange.binance.futures.model.enums.CandlestickInterval;
import com.univocity.trader.exchange.binance.futures.model.event.CandlestickEvent;
import com.univocity.trader.exchange.binance.futures.model.market.Candlestick;
import com.univocity.trader.exchange.binance.futures.model.market.ExchangeInfoEntry;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.utils.IncomingCandles;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


class BinanceFuturesExchange implements Exchange<Candlestick, Account> {

    private static final Logger log = LoggerFactory.getLogger(BinanceFuturesExchange.class);

    private SubscriptionClient socketClient;
    private SyncRequestClient restClient;

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    private final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 65536);

    private final Map<String, SymbolInformation> symbolInformation = new ConcurrentHashMap<>();

    private org.asynchttpclient.ws.WebSocket socketClientCloseable;

    private BinanceFuturesClientAccount binanceFuturesClientAccount;
    private char[] apiSecret;
    private String apiKey;
    private final double[] NO_PRICE = new double[]{-1.0};


    @Override
    public BinanceFuturesClientAccount connectToAccount(Account clientConfiguration) {
        this.apiKey = clientConfiguration.apiKey();
        this.apiSecret = clientConfiguration.secret();
        this.binanceFuturesClientAccount = new BinanceFuturesClientAccount(clientConfiguration.apiKey(), new String(clientConfiguration.secret()), this);
        return this.binanceFuturesClientAccount;
    }

    @Override
    public Candlestick getLatestTick(String symbol, TimeInterval interval) {
        return restClient().getCandlestick(symbol, CandlestickInterval.ONE_MINUTE, null, null, 1).get(0);
    }

    @Override
    public IncomingCandles<Candlestick> getLatestTicks(String symbol, TimeInterval interval) {
        try {
            return IncomingCandles.fromCollection(restClient().getCandlestick(symbol, CandlestickInterval.fromTimeInterval(interval), null, null, 5));
        } catch (Exception e) {
            throw new IllegalStateException("Error returnning latest ticks of " + symbol, e);
        }
    }

    @Override
    public IncomingCandles<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime) {
        //return IncomingCandles.fromCollection(restClient().getCandlestick(symbol, CandlestickInterval.fromTimeInterval(interval), 1000L, startTime, Long.valueOf(endTime).intValue()));
        return IncomingCandles.fromCollection(restClient().getCandlestick(symbol, CandlestickInterval.fromTimeInterval(interval),  startTime, endTime, 1500));
    }

    @Override
    public PreciseCandle generatePreciseCandle(Candlestick exchangeCandle) {
        return new PreciseCandle(
                exchangeCandle.getOpenTime(),
                exchangeCandle.getCloseTime(),
                new BigDecimal(exchangeCandle.getOpen()),
                new BigDecimal(exchangeCandle.getHigh()),
                new BigDecimal(exchangeCandle.getLow()),
                new BigDecimal(exchangeCandle.getClose()),
                new BigDecimal(exchangeCandle.getVolume())
        );
    }

    @Override
    public void startKeepAlive() {
        new KeepAliveUserDataStream(restClient()).start();
    }

    @Override
    public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candlestick> consumer) {
        CandlestickInterval interval = CandlestickInterval.fromTimeInterval(tickInterval);
        log.info("Opening Binance {} live stream for: {}", tickInterval, symbols);

        /*socketClientCloseable = socketClient().subscribeCandlestickEvent(symbols, interval, (data) -> {
            priceReceived(data.getSymbol(), data.getClose().doubleValue());
            consumer.tickReceived(data.getSymbol(), data);
        });*/
        socketClientCloseable = socketClient().subscribeCandlestickEvent(symbols, interval, new BinanceFuturesApiCallback<CandlestickEvent>() {
            @Override
            public void onResponse(CandlestickEvent response) {
                try {
                    priceReceived(response.getSymbol(), Double.parseDouble(response.getClose()));
                } catch (Exception e){
                    log.warn("Error updating latest price of " + response.getSymbol(), e);
                }

                consumer.tickReceived(response.getSymbol(), response);
            }

            public void onFailure(Throwable cause) {
                consumer.streamError(cause);
            }

            public void onClose() {
                consumer.streamClosed();
            }
        });

    }

    private final Map<String, double[]> latestPrices = new HashMap<>();

    private void priceReceived(String symbol, double price) {
        latestPrices.compute(symbol, (s, v) -> {
            if (v == null) {
                return new double[]{price};
            } else {
                v[0] = price;
                return v;
            }
        });
    }

    @Override
    public Map<String, double[]> getLatestPrices() {
        return Collections.emptyMap();
    }

    @Override
    public double getLatestPrice(String assetSymbol, String fundSymbol) {
        double price = latestPrices.getOrDefault(assetSymbol, NO_PRICE)[0];
        try {
            price = restClient().getSymbolPriceTicker(assetSymbol + fundSymbol).get(0).getPrice().doubleValue();
            priceReceived(assetSymbol + fundSymbol, price);
        } catch (Exception e) {
            log.error("Error getting latest price of " + assetSymbol + fundSymbol, e);
        }
        return price;
    }

    @Override
    public Map<String, SymbolInformation> getSymbolInformation() {
        if (symbolInformation.isEmpty()) {
            Map<String, ExchangeInfoEntry> symbols = restClient().getExchangeInformation().getSymbols().stream().collect(Collectors.toMap(ExchangeInfoEntry::getSymbol, s -> s));

            symbols.forEach((symbol, infoEntry) -> {
                SymbolInformation out = new SymbolInformation(symbol);
                out.quantityDecimalPlaces(infoEntry.getQuantityPrecision().intValue());
                out.priceDecimalPlaces(infoEntry.getPricePrecision().intValue());
                out.minimumAssetsPerOrder(infoEntry.getBaseAssetPrecision().intValue());
                symbolInformation.put(symbol, out);
            });

        }
        return symbolInformation;
    }


    private SubscriptionClient socketClient() {
        if (socketClient == null) {
            socketClient = BinanceApiInternalFactory.getInstance().createSubscriptionClient(apiKey, apiSecret == null ? null : new String(apiSecret), asyncHttpClient);
        }
        return socketClient;
    }

    @Override
    public void closeLiveStream() throws Exception {
        if (socketClientCloseable != null) {
            socketClientCloseable.sendCloseFrame();
            socketClientCloseable = null;
        }
    }

    private SyncRequestClient restClient() {
        if (restClient == null) {
            restClient = BinanceApiInternalFactory.getInstance().createSyncRequestClient(apiKey, apiSecret == null ? null : new String(apiSecret), new RequestOptions());
        }
        return restClient;
    }

    @Override
    public int historicalCandleCountLimit() {
        return 1000;
    }

}
