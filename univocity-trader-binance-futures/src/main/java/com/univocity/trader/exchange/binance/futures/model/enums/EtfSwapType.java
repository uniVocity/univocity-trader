package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

public enum  EtfSwapType {
  ETF_SWAP_IN("1"),
  ETF_SWAP_OUT("2");

  private final String code;

  EtfSwapType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<EtfSwapType> lookup = new EnumLookup<>(EtfSwapType.class);

  public static EtfSwapType lookup(String name) {
    return lookup.lookup(name);
  }
}
