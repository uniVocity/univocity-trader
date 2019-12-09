package com.univocity.trader.exchange.binance.api.client.impl;

import com.univocity.trader.exchange.binance.api.client.constant.*;
import com.univocity.trader.exchange.binance.api.client.security.*;
import org.apache.commons.lang3.*;
import org.asynchttpclient.*;
import org.asynchttpclient.extras.retrofit.*;

import java.util.stream.*;

public class BinanceCallCustomizer {

    private static final String eq = "=";

    public static void customize(String apiKey, String secret, AsyncHttpClientCallFactory.AsyncHttpClientCallFactoryBuilder builder) {
        if (StringUtils.isNotEmpty(apiKey) && StringUtils.isNotEmpty(secret)) {
            builder.callCustomizer(c -> c.requestCustomizer( r -> {
                Request original = r.build();
                boolean isApiKeyRequired = original.getHeaders().contains(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_APIKEY);
                boolean isSignatureRequired = original.getHeaders().contains(BinanceApiConstants.ENDPOINT_SECURITY_TYPE_SIGNED);

                // Endpoint requires sending a valid API-KEY
                if (isApiKeyRequired || isSignatureRequired) {
                    r.clearHeaders();
                    r.addHeader(BinanceApiConstants.API_KEY_HEADER, apiKey);
                    r.addHeader("Content-Type", "application/json");
                }
                // Endpoint requires signing the payload
                if (isSignatureRequired) {
                    String payload = original.getQueryParams().stream()
                            .map(e -> e.getName() + eq + e.getValue())
                            .collect(Collectors.joining("&"));
                    if (!StringUtils.isEmpty(payload)) {
                        String signature = HmacSHA256Signer.sign(payload, secret);
                        r.addQueryParam("signature", signature);
                    }
                }
            }));
        }
    }
}
