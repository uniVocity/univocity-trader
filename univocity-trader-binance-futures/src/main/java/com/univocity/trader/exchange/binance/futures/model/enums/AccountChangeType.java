package com.univocity.trader.exchange.binance.futures.model.enums;

import com.univocity.trader.exchange.binance.futures.impl.utils.EnumLookup;

/**
 * The event that Asset Change Notification Related ,for example : create order (order.place) ,
 * commit order (order.match),order refunds（order.refund),order canceled (order.cancel) ,card
 * deducts transaction fee （order.fee-refund),lever account transfer（margin.transfer),loan
 * principal（margin.loan),loan interest （margin.interest),return loan interest(margin.repay),other
 * asset change(other)
 */
public enum AccountChangeType {


  NEWORDER("order.place"),

  TRADE("order.match"),

  REFUND("order.refund"),

  CANCELORDER("order.cancel"),

  FEE("order.fee-refund"),

  TRANSFER("margin.transfer"),

  LOAN("margin.loan"),

  INTEREST("margin.interest"),

  REPAY("margin.repay"),

  OTHER("other"),

  INVALID("INVALID");

  private final String code;

  AccountChangeType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<AccountChangeType> lookup = new EnumLookup<>(
      AccountChangeType.class);

  public static AccountChangeType lookup(String name) {
    return lookup.lookup(name);
  }


}
