package com.univocity.trader.exchange.tdameritrade.model.Auth;

public class AccessToken {
    public enum grantType  {authorization_code, refresh_token}
    public String refreshToken;
    public String accessType;
    public String code;
    public String clientId;
    public String redirectURI;
}
