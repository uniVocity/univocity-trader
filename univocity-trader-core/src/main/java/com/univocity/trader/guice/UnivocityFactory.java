package com.univocity.trader.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.datasource.DataSourceFactory;

public class UnivocityFactory {
   private static UnivocityFactory instance = null;

   public static UnivocityFactory getInstance() {
      if (null == instance) {
         instance = new UnivocityFactory();
      }
      return instance;
   }

   private final Injector injector;

   private UnivocityFactory() {
      injector = Guice.createInjector(new UnivocityCoreModule());
   }

   public UnivocityConfiguration getUnivocityConfiguration() {
      return injector.getInstance(UnivocityConfiguration.class);
   }

   public DataSourceFactory getDataSourceFactory() {
      return injector.getInstance(DataSourceFactory.class);
   }
}
