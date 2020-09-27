package com.univocity.trader.exchange.binance.futures.model.enums;


public enum  TransferFuturesDirection {

  /**
   * FUTURES_TO_PRO,PRO_TO_FUTURES
   */
  FUTURES_TO_PRO("futures-to-pro"),
  PRO_TO_FUTURES("pro-to-futures")
  ;

  private String direction;

  TransferFuturesDirection(String direction) {
    this.direction = direction;
  }

  public String getDirection() {
    return direction;
  }

  public static TransferFuturesDirection find(String direction){
    for (TransferFuturesDirection d : TransferFuturesDirection.values()) {
      if (d.getDirection().equals(direction)) {
        return d;
      }
    }
    return null;
  }
}
