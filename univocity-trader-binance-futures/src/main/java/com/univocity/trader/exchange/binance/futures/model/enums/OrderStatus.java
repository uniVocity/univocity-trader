package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

/**
 * NEW 新建订单
 * PARTIALLY_FILLED 部分成交
 * FILLED 全部成交
 * CANCELED 已撤销
 * REJECTED 订单被拒绝
 * EXPIRED 订单过期(根据timeInForce参数规则)
 */
public enum OrderStatus {
  NEW("NEW"),
  PARTIALLY_FILLED("PARTIALLY_FILLED"),
  FILLED("FILLED"),
  CANCELED("CANCELED"),
  REJECTED("REJECTED"),
  EXPIRED("EXPIRED");


  private final String code;

  OrderStatus(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<OrderStatus> lookup = new EnumLookup<>(OrderStatus.class);

  public static OrderStatus lookup(String name) {
    return lookup.lookup(name);
  }
}
