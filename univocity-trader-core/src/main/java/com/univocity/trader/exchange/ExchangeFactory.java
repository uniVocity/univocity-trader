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
   public <T> Exchange<T> getExchange(String className)
         throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      final Class<?> clazz = Class.forName(className);
      if (null != clazz) {
         return (Exchange<T>) clazz.getDeclaredConstructor().newInstance();
      }
      return null;
   }
}
