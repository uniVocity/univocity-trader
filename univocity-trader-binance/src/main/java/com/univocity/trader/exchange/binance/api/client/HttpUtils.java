package com.univocity.trader.exchange.binance.api.client;

import io.netty.channel.*;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;

import java.time.*;

public abstract class HttpUtils {

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(10);

    /**
     * @param eventLoop
     * @return new instance of AsyncHttpClient for EventLoop
     */
    public static AsyncHttpClient newAsyncHttpClient(EventLoopGroup eventLoop, int maxFrameSize) {
        return newAsyncHttpClient(eventLoop, maxFrameSize, null);
    }

    public static AsyncHttpClient newAsyncHttpClient(EventLoopGroup eventLoop, int maxFrameSize, ProxyServer proxyServer) {
        DefaultAsyncHttpClientConfig.Builder config = Dsl.config()
                .setEventLoopGroup(eventLoop)
                .setProxyServer(proxyServer)
                .addChannelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(DEFAULT_CONNECTION_TIMEOUT.toMillis()))
                .setWebSocketMaxFrameSize(maxFrameSize);
        return Dsl.asyncHttpClient(config);
    }
}