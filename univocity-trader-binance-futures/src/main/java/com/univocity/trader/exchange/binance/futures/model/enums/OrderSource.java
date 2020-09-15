package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

/**
 * sys, web, api, app.
 */
public enum OrderSource {
  SYS("sys"),
  WEB("web"),
  API("api"),
  APP("app"),
  FLSYS("fl-sys"),
  FLMGT("fl-mgt"),
  SPOTWEB("spot-web"),
  SPOTAPI("spot-api"),
  SPOTAPP("spot-app"),
  MARGINAPI("margin-api"),
  MARGINWEB("margin-web"),
  MARGINAPP("margin-app"),
  SUPERMARGINAPI("super_margin_api"),
  SUPERMARGINAPP("super_margin_app"),
  SUPERMARGINWEB("super_margin_web"),
  SUPERMARGINFLSYS("super_margin_fl_sys"),
  SUPERMARGINFLMGT("super_margin_fl_mgt");

  private final String code;

  OrderSource(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<OrderSource> lookup = new EnumLookup<>(OrderSource.class);

  public static OrderSource lookup(String name) {
    return lookup.lookup(name);
  }
}
