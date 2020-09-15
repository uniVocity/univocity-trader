package com.univocity.trader.exchange.binance.futures;

import com.univocity.trader.exchange.binance.futures.exception.BinanceApiException;

/**
 * The error handler for the subscription.
 */
@FunctionalInterface
public interface SubscriptionErrorHandler {

  void onError(BinanceApiException exception);
}
