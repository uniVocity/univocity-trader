package com.univocity.trader.exchange.tdameritrade.impl;

import com.univocity.trader.exchange.tdameritrade.exception.TDAmeritradeApiException;
import com.univocity.trader.exchange.tdameritrade.impl.utils.JsonWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApiInvoker {

    private static final Logger log  = LoggerFactory.getLogger(RestApiInvoker.class);
    private static final OkHttpClient client = new OkHttpClient();

    static void checkResponse(JsonWrapper json){
        // check for error here and throw Exceptions as needed
    }

    static <T> T callSync(RestApiRequest<T> request) {
        try {
            String responseBody;
            log.debug("[TD-Ameritrade] Request URL " + request.request.url());
            Response response = client.newCall(request.request).execute();
            if (response != null && response.body() != null) {
                responseBody = response.body().string();
                response.close();
            } else {
                throw new TDAmeritradeApiException(TDAmeritradeApiException.ENV_ERROR,
                        "[TD-Ameritrade] [Invoking] Cannot get the response from server");
            }
            log.debug("[TD-Ameritrade] Response ====> {}", responseBody);
            JsonWrapper jsonWrapper = JsonWrapper.parseFromString(responseBody);
            checkResponse(jsonWrapper);
//            return request.jsonParser.parseJson(jsonWrapper);
        } catch (TDAmeritradeApiException e){
            throw e;
        } catch (Exception e) {
            throw new TDAmeritradeApiException(TDAmeritradeApiException.ENV_ERROR,
                    "[TD-Ameritrade] [Invoking] Unexpected error: " + e.getMessage());
        }
        return null;
    }
}
