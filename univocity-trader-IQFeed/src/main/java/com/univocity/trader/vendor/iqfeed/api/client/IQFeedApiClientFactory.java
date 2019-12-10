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


    private IQFeedApiClientFactory(String iqPortalPath, String product, String version, String login, String password,
                                   boolean autoconnect, boolean savelogin, AsyncHttpClient client){
        this.iqPortalPath = iqPortalPath;
        this.product = product;
        this.version = version;
        this.login = login;
        this.password = password;
        this.autoconnect = autoconnect;
        this.savelogin = savelogin;
        this.client = client;
    }

    public static IQFeedApiClientFactory newInstance(String iqPortalPath, String product, String version, String login,
                                                     String password, boolean autoconnect, boolean savelogin, AsyncHttpClient client){
        return new IQFeedApiClientFactory(iqPortalPath, product, version, login, password, autoconnect, savelogin, client);
    }

    public IQFeedApiWebSocketClient newWebSocketClient(String host, String port) {
        return new IQFeedApiWebSocketClientImpl(this.client, host, port, new IQFeedApiWebSocketListener<>());
    }

}