package com.univocity.trader.exchange;

/**
 * @author tom@khubla.com
 */
public class ExchangeFactory {
   private static ExchangeFactory instance;

   public static ExchangeFactory getInstance() {
      if (null == instance) {
         instance = new ExchangeFactory();
      }
      return instance;
   }

   // some day, Guice
   public <T> Exchange<T> getExchange(Class<?> clazz) {
      if (null != clazz) {
         try {
            return (Exchange<T>) clazz.getDeclaredConstructor().newInstance();
         } catch (Exception e) {
            return null;
         }
      }
      return null;
   }
}
