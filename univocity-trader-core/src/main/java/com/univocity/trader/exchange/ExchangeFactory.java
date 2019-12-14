package com.univocity.trader.exchange;

import java.lang.reflect.InvocationTargetException;

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
   public <T> Exchange<T> getExchange(Class<?> clazz)
         throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      if (null != clazz) {
         return (Exchange<T>) clazz.getDeclaredConstructor().newInstance();
      }
      return null;
   }
}
