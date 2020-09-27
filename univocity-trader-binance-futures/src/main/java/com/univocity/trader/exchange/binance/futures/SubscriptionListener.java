package com.univocity.trader.exchange.binance.futures;

/**
 * You must implement the SubscriptionListener interface. <br> The server will push any update to
 * the client. if client get the update, the onReceive method will be called.
 *
 * @param <T> The type of received data.
 */
@FunctionalInterface
public interface SubscriptionListener<T> {

  /**
   * onReceive will be called when get the data sent by server.
   *
   * @param data The data send by server.
   */
  void onReceive(T data);
}
