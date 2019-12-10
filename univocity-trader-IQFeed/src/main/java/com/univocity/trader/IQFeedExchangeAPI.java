package com.univocity.trader;

import com.univocity.trader.*;
import com.univocity.trader.candles.*;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.vendor.iqfeed.api.client.*;
import com.univocity.trader.candles.SymbolInformation;
import com.univocity.trader.vendor.iqfeed.api.client.*;
import com.univocity.trader.vendor.iqfeed.api.client.constant.IQFeedApiConstants;
import com.univocity.trader.vendor.iqfeed.api.client.domain.market.Candlestick;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.IQFeedHistoricalRequest;
import com.univocity.trader.vendor.iqfeed.api.client.domain.request.IQFeedHistoricalRequestBuilder;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import org.asynchttpclient.*;
import org.slf4j.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class IQFeedExchangeAPI implements ExchangeApi<Candlestick> {

    private static final Logger logger = LoggerFactory.getLogger(IQFeedExchangeAPI.class);

    private IQFeedApiWebSocketClient socketClient;
    private org.asynchttpclient.ws.WebSocket socketClientCloseable;
    private final Map<String, SymbolInformation> symbolInformation = new ConcurrentHashMap<>();

    private boolean iqPortal = false;
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    // TODO: ask about maxFrameSize
    private final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 655356);
    private String iqPortalPath = IQFeedApiConstants.IQPORTAL_PATH;
    private String host;
    private String port;
    private String product;
    private String version;
    private String login;
    private String pass;

    public IQFeedClientAccountApi connectToIQFeedAccount(String host, String port, String product, String version, String login, String pass, boolean autoConnect,
                                                   boolean saveLoginInfo ){
        this.host = host;
        this.port = port;
        this.product = product;
        this.version = version;
        this.login = login;
        this.pass = pass;

        if(!iqPortal){
            try {
                Runtime.getRuntime().exec(iqPortalPath, null, new File(iqPortalPath));
                iqPortal = true;
            } catch(Exception e){
                logger.info(e.getMessage());
            }
        }
        return new IQFeedClientAccountApi(iqPortalPath, product, host, port, version, login, pass, this,
                true, true, asyncHttpClient);
    }


    @Override
    public IQFeedClientAccountApi connectToAccount(String api, String secret){
        if(!iqPortal){
            try {
                Runtime.getRuntime().exec(iqPortalPath, null, new File(iqPortalPath));
                iqPortal = true;
            } catch (Exception e){
                logger.info(e.getMessage());
            }
        }
        return null;
    }

    // TODO: implement
    @Override
    public Map<String, SymbolInformation> getSymbolInformation(){
      return new HashMap<>();
    };

    @Override
    public Candlestick getLatestTick(String symbol, TimeInterval interval){
        // TODO: implement
        return null;
    }

    @Override
    public Map<String, Double> getLatestPrices(){
        return new HashMap<>();
    }

    @Override
    public double getLatestPrice(String assetSymbol, String fundSymbol){
        return new Double(0);
    }

    @Override
    public List<Candlestick> getLatestTicks(String symbol, TimeInterval interval){
        // TODO: implement
        ChronoUnit timeUnit  = null;
        switch(TimeInterval.getUnitStr(interval.unit)){
            case "d":
                timeUnit = ChronoUnit.DAYS;
                break;
            case "h":
                timeUnit = ChronoUnit.HOURS;
                break;
            case "m":
                timeUnit = ChronoUnit.MINUTES;
                break;
            case "s":
                timeUnit = ChronoUnit.SECONDS;
                break;
            case "ms":
                timeUnit = ChronoUnit.MILLIS;
                break;
        }
        StringBuilder requestIDBuilder = new StringBuilder("IQFeedLatestTicksRequest_" + Instant.now().toString());
        requestIDBuilder.append("_symbol:" + symbol + "_interval:" + interval.toString());
        IQFeedHistoricalRequest request = new IQFeedHistoricalRequestBuilder()
                .setRequestID(requestIDBuilder.toString())
                .setSymbol(symbol)
                .setIntervalType(interval)
                .setBeginDateTime(Instant.now().minus(100L, timeUnit).toEpochMilli())
                .setEndDateTime(Instant.now().toEpochMilli())
                .build();

        List<Candlestick> candles = socketClient().getHistoricalCandlestickBars(request);
        return candles;
    }

    //TODO: implement
    @Override
    public List<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime){
        StringBuilder requestIDBuilder = new StringBuilder("IQFeedHistoricalRequest_" + Instant.now().toString());
        requestIDBuilder.append("_symbol:" + symbol+ "_interval:" + interval.toString() + "_start:" + startTime + "_end:" + endTime);
        IQFeedHistoricalRequest request = new IQFeedHistoricalRequestBuilder()
                .setRequestID(requestIDBuilder.toString())
                .setSymbol(symbol)
                .setIntervalType(interval)
                .setBeginDateTime(startTime)
                .setEndDateTime(endTime)
                .build();
        return socketClient.getHistoricalCandlestickBars(request);
    }
    // TODO: add callback for connection login via IQFeed

    @Override
    public Candle generateCandle(Candlestick c) {
        return new Candle(
                c.getOpenTime(),
                c.getCloseTime(),
                Double.parseDouble(c.getOpen()),
                Double.parseDouble(c.getHigh()),
                Double.parseDouble(c.getLow()),
                Double.parseDouble(c.getClose()),
                Double.parseDouble(c.getVolume())
        );
    }

    public PreciseCandle generatePreciseCandle(Candlestick c){
        return new PreciseCandle(
                c.getOpenTime(),
                c.getCloseTime(),
                new BigDecimal(c.getOpen()),
                new BigDecimal(c.getHigh()),
                new BigDecimal(c.getLow()),
                new BigDecimal(c.getClose()),
                new BigDecimal(c.getVolume()));
    }

//    @Override
//    public List<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime){
//        return socketClient().getC
//    }

    @Override
    public TimeInterval handleException(String action, String symbol, Exception e){
        String message = "excecute " + action + " for " + symbol;
        if(e.getCause() instanceof TimeoutException){
            logger.error("Timeout trying to ", message, e);
        } else if (e.getCause() instanceof UnknownHostException) {
            logger.error("Unable to " + message + ". IQFeed is offline. ", e);
            return TimeInterval.minutes(1);
        } else {
            logger.error("Eror trying to " + message, e);
        }
        return null;
    }

    @Override
    public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candlestick> consumer){
    }

    @Override
    public void closeLiveStream(){
        if(socketClientCloseable != null){
            socketClientCloseable.sendCloseFrame();
            socketClientCloseable = null;
        }
    }


    private IQFeedApiWebSocketClient socketClient(){
        if(socketClient == null){
            IQFeedApiClientFactory factory = IQFeedApiClientFactory.newInstance(
                    IQFeedApiConstants.IQPORTAL_PATH,
            IQFeedApiConstants.IQPRODUCT,
            IQFeedApiConstants.IQVERSION,
            IQFeedApiConstants.IQLOGIN,
            IQFeedApiConstants.IQPASS,
            true, true, asyncHttpClient);
            socketClient = factory.newWebSocketClient(IQFeedApiConstants.HOST, IQFeedApiConstants.PORT);
        }
        return socketClient;
    }

}

