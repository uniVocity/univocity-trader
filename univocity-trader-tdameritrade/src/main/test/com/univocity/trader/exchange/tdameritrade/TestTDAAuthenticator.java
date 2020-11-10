package com.univocity.trader.exchange.tdameritrade;

import com.univocity.trader.exchange.tdameritrade.api.auth.TDAAuthenticator;
import org.junit.Test;

public class TestTDAAuthenticator {


    @Test
    public void testAuth(){
        String redirectURL = "https://127.0.0.1:8080";
        String driverPath = "src\\main\\java\\com\\univocity\\trader\\exchange\\tdameritrade\\utils\\geckodriver.exe";
        String consumerKey = "3RRAXALGIMFS42LCPCVO9GVKMVAMVOW2";
        TDAAuthenticator authenticator = new TDAAuthenticator(redirectURL, consumerKey, driverPath);
        String authCode = authenticator.authenticateAndGetAuthCode();
        System.out.println(authCode);

        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(options);
        syncRequestClient.postAccessToken();
    }
}
