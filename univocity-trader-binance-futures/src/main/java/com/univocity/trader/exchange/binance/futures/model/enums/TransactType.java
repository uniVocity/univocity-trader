package com.univocity.trader.exchange.binance.futures.model.enums;

public enum  TransactType {

  TRADE("trade"),
  ETF("etf"),
  TRANSACT_FEE("transact-fee"),
  FEE_DEDUCTION("fee-deduction"),
  TRANSFER("transfer"),
  CREDIT("credit"),
  LIQUIDATION("liquidation"),
  INTEREST("interest"),
  DEPOSIT("deposit"),
  WITHDRAW("withdraw"),
  WITHDRAW_FEE("withdraw-fee"),
  EXCHANGE("exchange"),
  OTHER_TYPES("other-types")

  ;
  private final String code;

  TransactType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static TransactType find(String code) {
    for (TransactType transactType : TransactType.values()) {
      if (transactType.getCode().equals(code)) {
        return transactType;
      }
    }
    return null;
  }

}
