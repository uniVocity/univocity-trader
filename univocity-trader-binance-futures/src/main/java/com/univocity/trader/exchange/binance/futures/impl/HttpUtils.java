package com.univocity.trader.exchange.binance.futures.impl;

import com.univocity.trader.config.PropertyBasedConfiguration;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.proxy.ProxyServer;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    private static final long REQUEST_TIMEOUT_MS = 1000;
    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final PropertyBasedConfiguration propertyConfiguration = new PropertyBasedConfiguration.AnyOneBuilder().build("nwscu.properties", "config/nwscu.properties");

    public static OkHttpClient newOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //代理服务器的IP和端口号

        if(propertyConfiguration.containsAnyProperties("nwsuc.proxy.host", "nwsuc.proxy.port")) {
            String host = propertyConfiguration.getProperty("nwsuc.proxy.host");
            Integer port = propertyConfiguration.getInteger("nwsuc.proxy.port");

            //此处添加代理服务设置
            if(StringUtils.isNotBlank(host) && port != null){
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
            }
        }
        //builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1087)));
        OkHttpClient okHttpClient = builder
                //设置读取超时时间
                .readTimeout(REQUEST_TIMEOUT_MS, TimeUnit.SECONDS)
                //设置写的超时时间
                .writeTimeout(REQUEST_TIMEOUT_MS, TimeUnit.SECONDS)
                .connectTimeout(REQUEST_TIMEOUT_MS, TimeUnit.SECONDS).build();

        return okHttpClient;
    }

    /**
     * @param eventLoop
     * @return new instance of AsyncHttpClient for EventLoop
     */
    public static AsyncHttpClient newAsyncHttpClient(EventLoopGroup eventLoop, int maxFrameSize) {
        DefaultAsyncHttpClientConfig.Builder config = Dsl.config()
                .setEventLoopGroup(eventLoop)
                .addChannelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(DEFAULT_CONNECTION_TIMEOUT.toMillis()))
                .setWebSocketMaxFrameSize(maxFrameSize);

        if(propertyConfiguration.containsAnyProperties("nwsuc.proxy.host", "nwsuc.proxy.port")) {
            String host = propertyConfiguration.getProperty("nwsuc.proxy.host");
            Integer port = propertyConfiguration.getInteger("nwsuc.proxy.port");

            //此处添加代理服务设置
            if(StringUtils.isNotBlank(host) && port != null){
                config.setProxyServer(new ProxyServer.Builder(host, port));
            }
        }
        return Dsl.asyncHttpClient(config);
    }

}
