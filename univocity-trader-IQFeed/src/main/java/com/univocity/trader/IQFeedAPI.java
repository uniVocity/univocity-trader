package com.univocity.trader;

import com.univocity.trader.candles.SymbolInformation;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IQFeedAPI implements ExchangeApi<Candlestick> {
    private static final Logger log = LoggerFactory.getLogger(BinanceExchangeApi.class);

    private IQFeedAPIWebSocketClient socketClient;
    private org.asynchttpclient.ws.WebSocket socketClientCloseable;
    private final Map<String, SymbolInformation> symbolInformation = new ConcurrentHashMap<>();

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    // TODO: ask about maxFrameSize
    private final AsyncHttpClient asyncHttpClient = HttpUtils.newAsyncHttpClient(eventLoopGroup, 655356);

    // TODO: add callback for connection login via IQFeed

//    @Override
//    public List<Candlestick> getHistoricalTicks(String symbol, TimeInterval interval, long startTime, long endTime){
//        return socketClient().getC
//    }


    private IQFeedAPIWebSocketClient socketClient(){
        if(socketClient == null){
            IQFeedAPIClientFactory factory = IQFeedAPIClientFactory.newInstance(asyncHttpClient);
            socketClient = factory.newWebSocketClient();
        }
        return socketClient;
    }

}

