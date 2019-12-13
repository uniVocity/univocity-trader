package com.univocity.trader.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;
import com.univocity.trader.datasource.DataSourceFactory;
import com.univocity.trader.datasource.impl.ThreadLocalDataSourceFactoryImpl;

/**
 * @author tom@khubla.com
 */
public class UnivocityCoreModule implements Module {
   public UnivocityCoreModule() {
   }

   @Override
   public void configure(Binder binder) {
      /*
       * config
       */
      binder.bind(UnivocityConfiguration.class).to(ConfigFileUnivocityConfigurationImpl.class);
      /*
       * data source
       */
      binder.bind(DataSourceFactory.class).to(ThreadLocalDataSourceFactoryImpl.class);
   }
}