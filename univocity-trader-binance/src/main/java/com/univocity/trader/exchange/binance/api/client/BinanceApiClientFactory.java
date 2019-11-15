package com.univocity.trader.exchange.binance.api.client;

import com.univocity.trader.exchange.binance.api.client.impl.*;
import org.asynchttpclient.*;

/**
 * A factory for creating BinanceApi client objects.
 */
public class BinanceApiClientFactory {

  /**
   * API Key
   */
  private String apiKey;

  /**
   * Secret.
   */
  private String secret;

  private AsyncHttpClient client;

  /**
   * Instantiates a new binance api client factory.
   *
   * @param apiKey the API key
   * @param secret the Secret
   */
  private BinanceApiClientFactory(String apiKey, String secret, AsyncHttpClient client) {
    this.apiKey = apiKey;
    this.secret = secret;
    this.client = client;
  }

  /**
   * New instance.
   *
   * @param apiKey the API key
   * @param secret the Secret
   *
   * @return the binance api client factory
   */
  public static BinanceApiClientFactory newInstance(String apiKey, String secret, AsyncHttpClient client) {
    return new BinanceApiClientFactory(apiKey, secret, client);
  }

  /**
   * New instance without authentication.
   *
   * @return the binance api client factory
   */
  public static BinanceApiClientFactory newInstance(AsyncHttpClient client) {
    return new BinanceApiClientFactory(null, null, client);
  }

  /**
   * Creates a new synchronous/blocking REST client.
   */
  public BinanceApiRestClient newRestClient() {
    return new BinanceApiRestClientImpl(apiKey, secret, this.client);
  }

  /**
   * Creates a new asynchronous/non-blocking REST client.
   */
  public BinanceApiAsyncRestClient newAsyncRestClient() {return new BinanceApiAsyncRestClientImpl(apiKey, secret, this.client);
  }

  /**
   * Creates a new web socket client used for handling data streams.
   */
  public BinanceApiWebSocketClient newWebSocketClient() {
    return new BinanceApiWebSocketClientImpl(this.client);
  }
}
