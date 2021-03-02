package com.univocity.trader.exchange.binance.api.client;

import com.univocity.trader.exchange.binance.api.client.impl.*;
import com.univocity.trader.exchange.binance.api.client.config.*;
import org.asynchttpclient.*;

/**
 * A factory for creating BinanceApi client objects.
 */
public class BinanceApiClientFactory {

  /**
   * Api Configuration to create the clients
   */
  private final BinanceApiConfig apiConfig;

  /**
   * Instantiates a new binance api client factory.
   *
   * @param apiKey the API key
   * @param secret the Secret
   */
  private BinanceApiClientFactory(String apiKey, String secret, AsyncHttpClient client, boolean isTestNet) {
    this.apiConfig = new BinanceApiConfig(apiKey, secret, client, isTestNet);
  }

  /**
   * New instance.
   *
   * @param apiKey the API key
   * @param secret the Secret
   * @param client the httpClient
   * @param isTestNet if it uses the Test Network or Real Network
   *
   * @return the binance api client factory
   */
  public static BinanceApiClientFactory newInstance(String apiKey, String secret, AsyncHttpClient client, boolean isTestNet) {
    return new BinanceApiClientFactory(apiKey, secret, client, isTestNet);
  }

  /**
   * New instance.
   *
   * @param apiKey the API key
   * @param secret the Secret
   * @param client the httpClient
   *
   * @return the binance api client factory
   */

  public static BinanceApiClientFactory newInstance(String apiKey, String secret, AsyncHttpClient client) {
      return newInstance(apiKey, secret, client, false);
  }

  /**
   * New instance without authentication.
   *
   * @return the binance api client factory
   */
  public static BinanceApiClientFactory newInstance(AsyncHttpClient client) {
    return new BinanceApiClientFactory(null, null, client, false);
  }

  /**
   * Creates a new synchronous/blocking REST client.
   */
  public BinanceApiRestClient newRestClient() {
    return new BinanceApiRestClientImpl(apiConfig);
  }

  /**
   * Creates a new asynchronous/non-blocking REST client.
   */
  public BinanceApiAsyncRestClient newAsyncRestClient() {return new BinanceApiAsyncRestClientImpl(apiConfig);
  }

  /**
   * Creates a new web socket client used for handling data streams.
   */
  public BinanceApiWebSocketClient newWebSocketClient() {
    return new BinanceApiWebSocketClientImpl(apiConfig.getAsyncHttpClient());
  }

}
