package com.univocity.trader;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.candles.TickConsumer;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.vendor.iqfeed.api.client.IQFeedApiCallback;
import com.univocity.trader.vendor.iqfeed.api.client.constant.IQFeedConstants;
import com.univocity.trader.vendor.iqfeed.api.client.impl.IQFeedAPIWebSocketClient;
import com.univocity.trader.candles.SymbolInformation;
import com.univocity.trader.vendor.iqfeed.api.client.requests.IQFeedHistoricalRequest;
import com.univocity.trader.vendor.iqfeed.api.client.requests.IQFeedHistoricalRequestBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class IQFeedExchangeAPI implements ExchangeApi<Candlestick> {

    private static final Logger log = LoggerFactory.getLogger(BinanceExchangeApi.class);

    private IQFeedAPIWebSocketClient socketClient;
    private org.asynchttpclient.ws.WebSocket socketClientCloseable;
    private final Map<String, SymbolInformation> symbolInformation = new ConcurrentHashMap<>();

    private boolean IQPortal = False;
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    // TODO: ask about maxFrameSize
    private final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 655356);

    @Override
    public IQFeedAccountApi connectToAccount(String api){
        // TODO: implement this
        if(!IQPortal){
            try {
                String IQPortalPath = IQFeedConstants.getIQPortalPath();
                Runtime.getRuntime().exec(IQPortalPath, null, new File(IQPortalPath));
                IQPortal = True;
            } catch (Exception e){
                logger.log(e.getMessage());
            }
            return new IQFeedClientAccountApi();
        }
    }

    @Override
    public Candlestick getLatestTick(String symbol, TiemInterval interval){
        // TODO: implement
        List<Candlestick> candles = socketClient().getCandlestickBars(symbol, CandlestickInterval.fromTimeInterval(interval), 1, null, null);
        if(candles != null && candles.size() > 0) {
            return candles.get(0);
        }
        return null;
    }

    //TODO: implement
    @Override
    public List<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime){
        IQFeedHistoricalRequest request = new IQFeedHistoricalRequestBuilder()
                .setRequestID(requestID)
                .setSymbol(symbol)
                .setIntervalType(interval)
                .setBeginDateTime(startTime)
                .setEndDateTime(endTime)
                .build();

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

//    @Override
//    public List<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime){
//        return socketClient().getC
//    }

    @Override
    public TimeInterval handleException(String action, String symbol, Exception e){
        String message = "excecute " + action + " for " + symbol;
        if(e.getCause() instanceof TimeoutException){
            log.error("Timeout trying to ", message, e);
        } else if (e.getCause() instanceof UnknownHostException) {
            log.error("Unable to " + message + ". IQFeed is offline. ", e);
            return TimeInterval.minutes(1);
        } else {
            log.error("Eror trying to " + message, e);
        }
        return null;
    }

    @Override
    public void openLiveStream(String symbols, TimeInterval tickInterval, TickConsumer<Candlestick> consumer){
        CandlestickInterval interval = CandlestickInterval.fromTimeInterval(tickInterval);
        log.info("Opening IQFeed {} live stream for: {}", tickInterval, symbols);
        socketClientCloseable = socketClient().onCandlestickEvent(symbols, interval, new IQFeedApiCallback<>(){

            @Override
            public void onResponse(CandlestickEvent response){consumer.tickReceived(response.getSymbol(), response);}

            public void onFailure(Throwable cause) { consumer.streamError(cause);}

            public void onClose() { consumer.streamClosed(); }
        });
    }

    @Override
    public void closeLiveStream(){
        if(socketClientCloseable != null){
            socketClientCloseable.sendCloseFrame();
            socketClientCloseable = null;
        }
    }

    @Override
    public Map<String, Double>

    private IQFeedAPIWebSocketClient socketClient(){
        if(socketClient == null){
            IQFeedAPIClientFactory factory = IQFeedAPIClientFactory.newInstance(asyncHttpClient);
            socketClient = factory.newWebSocketClient();
        }
        return socketClient;
    }

}

