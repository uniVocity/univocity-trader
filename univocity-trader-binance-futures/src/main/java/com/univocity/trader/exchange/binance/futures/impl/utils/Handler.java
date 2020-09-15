package com.univocity.trader.exchange.binance.futures.impl.utils;

@FunctionalInterface
public interface Handler<T> {

  void handle(T t);
}
