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


    private IQFeedApiClientFactory(AsyncHttpClient client){
        this.client = client;
    }

    public static IQFeedApiClientFactory newInstance(AsyncHttpClient client) {
        return new IQFeedApiClientFactory(client);
    }

    public IQFeedApiWebSocketClient newWebSocketClient() {
        return new IQFeedApiWebSocketClientImpl(client, new IQFeedApiWebSocketListener<>());
    }

}