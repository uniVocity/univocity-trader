package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;


/**
 * withdraw, deposit.
 */
public enum WithdrawState {


  SUBMITTED("submitted"),
  REEXAMINE("reexamine"),
  CANCELED("canceled"),
  PASS("pass"),
  REJECT("reject"),
  PRETRANSFER("pre-transfer"),
  WALLETTRANSFER("wallet-transfer"),
  WALEETREJECT("wallet-reject"),
  CONFIRMED("confirmed"),
  CONFIRMERROR("confirm-error"),
  REPEALED("repealed");


  private final String code;

  WithdrawState(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<WithdrawState> lookup = new EnumLookup<>(WithdrawState.class);

  public static WithdrawState lookup(String name) {
    return lookup.lookup(name);
  }

}
