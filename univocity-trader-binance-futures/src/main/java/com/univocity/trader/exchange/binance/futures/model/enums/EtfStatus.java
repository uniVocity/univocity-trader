package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

public enum EtfStatus {
  NORMAL("1"),
  REBALANCING_START("2"),
  CREATION_AND_REDEMPTION_SUSPEND("3"),
  CREATION_SUSPEND("4"),
  REDEMPTION_SUSPEND("5");

  private final String code;

  EtfStatus(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<EtfStatus> lookup = new EnumLookup<>(EtfStatus.class);

  public static EtfStatus lookup(String name) {
    return lookup.lookup(name);
  }
}
