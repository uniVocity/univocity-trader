package com.univocity.trader.strategy;

import java.util.function.Supplier;

/**
 * @author tom@khubla.com
 */
public class StrategyFactory {
   private static StrategyFactory instance;

   public static StrategyFactory getInstance() {
      if (null == instance) {
         instance = new StrategyFactory();
      }
      return instance;
   }

   // some day, Guice
   public Supplier<StrategyMonitor> getStrategyMonitorSupplier(Class<?> clazz) {
      if (null != clazz) {
         final Supplier<StrategyMonitor> ret = () -> {
            try {
               return (StrategyMonitor) clazz.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
               return null;
            }
         };
         return ret;
      }
      return null;
   }

   // some day, Guice
   public Supplier<Strategy> getStrategySupplier(Class<?> clazz) {
      if (null != clazz) {
         final Supplier<Strategy> ret = () -> {
            try {
               return (Strategy) clazz.getDeclaredConstructor().newInstance();
            } catch (final Exception e) {
               return null;
            }
         };
         return ret;
      }
      return null;
   }
}
