package com.univocity.trader.currency;

/**
 * @author tom@khubla.com
 */
public class Currency {
   public static enum CurrencyType {
      crypto, fiat, reference
   }

   private final String symbol;
   private final CurrencyType type;
   private final String description;;

   public Currency(String symbol, CurrencyType type, String description) {
      super();
      this.symbol = symbol;
      this.description = description;
      this.type = type;
   }

   public String getDescription() {
      return description;
   }

   public String getSymbol() {
      return symbol;
   }

   public CurrencyType getType() {
      return type;
   }
}