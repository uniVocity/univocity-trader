package com.univocity.trader.exchange.binance.futures.model.enums;

public enum MBPLevelEnums {


  LEVEL5(5),
  LEVEL10(10),
  LEVEL20(20),
  LEVEL150(150),

  ;

  private final int levels;

  MBPLevelEnums(int levels) {
    this.levels = levels;
  }

  public int getLevels() {
    return levels;
  }
}
