package com.univocity.trader.vendor.iqfeed.api.client;

import com.univocity.trader.vendor.iqfeed.api.client.impl.IQFeedApiWebSocketClientImpl;
import com.univocity.trader.vendor.iqfeed.api.client.impl.IQFeedApiWebSocketListener;
import org.asynchttpclient.AsyncHttpClient;

public class IQFeedApiClientFactory {

    private String iqPortalPath;
    private String product;
    private String version;
    private String login;
    private String password;
    private boolean autoconnect;
    private boolean savelogin;
    private AsyncHttpClient client;


    private IQFeedApiClientFactory(){
    }

    public static IQFeedApiClientFactory newInstance() {
        return new IQFeedApiClientFactory();
    }

    public IQFeedApiWebSocketClient newWebSocketClient(String host, String port) {
        return new IQFeedApiWebSocketClientImpl(this.client, host, port, new IQFeedApiWebSocketListener<>());
    }

}