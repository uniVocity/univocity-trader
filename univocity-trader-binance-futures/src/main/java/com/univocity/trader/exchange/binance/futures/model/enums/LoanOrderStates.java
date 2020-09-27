package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

/**
 * created, accrual, cleared, invalid.
 */
public enum LoanOrderStates {

  CREATED("created"),
  ACCRUAL("accrual"),
  CLEARED("cleared"),
  INVALID("invalid");

  private final String code;

  LoanOrderStates(String state) {
    this.code = state;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<LoanOrderStates> lookup = new EnumLookup<>(LoanOrderStates.class);

  public static LoanOrderStates lookup(String name) {
    return lookup.lookup(name);
  }
}
