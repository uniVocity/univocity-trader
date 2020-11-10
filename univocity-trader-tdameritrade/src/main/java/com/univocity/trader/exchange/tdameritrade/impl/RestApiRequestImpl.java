package com.univocity.trader.exchange.tdameritrade.impl;

import com.univocity.trader.exchange.tdameritrade.RequestOptions;
import com.univocity.trader.exchange.tdameritrade.impl.utils.UrlParamsBuilder;
import com.univocity.trader.exchange.tdameritrade.model.Auth.AccessToken;
import com.univocity.trader.exchange.tdameritrade.model.Auth.EASObject;
import okhttp3.Request;

public class RestApiRequestImpl {

    private String serverUrl;

    public RestApiRequestImpl(RequestOptions options) {
        this.serverUrl = options.getUrl();
    }

    RestApiRequest<EASObject> postAccessToken(String grantType, String refreshToken, String accessType, String code, String client_id, String redirect_uri){
        RestApiRequest<AccessToken> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("grant_type", grantType)
                .putToUrl("refresh_token", refreshToken)
                .putToUrl("access_type", accessType)
                .putToUrl("code", code)
                .putToUrl("client_id", client_id)
                .putToUrl("redirect_uri", redirect_uri);

        request.request = createRequestByPost("/v1/oauth2/token", builder);

        request.jsonParser = (jsonWrapper -> {
            EASObject result = new EASObject();
            result.setAccessToken(jsonWrapper.getString("access_token"));
            result.setRefreshToken(jsonWrapper.getString("refresh_token"));
            result.setTokenType(jsonWrapper.getString("token_type"));
            result.setScope(jsonWrapper.getString("scope"));
            result.setExpiresIn(jsonWrapper.getLong("expires_in"));
            result.setRefreshTokenExpiresIn(jsonWrapper.getLong("refresh_token_expires_in"));
        });
    }

    private Request createRequestByPost(String address, UrlParamsBuilder builder){
        return this.createRequest(this.serverUrl, address, builder);
    }

    private Request createRequest(String url, String address, UrlParamsBuilder builder){
        String requestUrl = url + address;
        System.out.println(requestUrl);
        if(builder != null){
            if(builder.hasPostParam()){
//                return new Request.Builder().url(requestUrl + builder.buildUrl())
//                        .addHeader("Content-Type", "application/x-www-form-urlencoded").build();
            } else {
                return new Request.Builder().url(requestUrl + builder.buildUrl())
                        .addHeader("Content-Type", "application/x-www-form-urlencoded").build();
            }
        } else {
            return new Request.Builder().url(requestUrl).addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
        }
    }
}
