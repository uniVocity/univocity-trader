package com.univocity.trader.datasource.impl;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.datasource.DataSourceFactory;
import com.univocity.trader.factory.UnivocityFactory;

/**
 * @author tom@khubla.com
 */
public class ThreadLocalDataSourceFactoryImpl implements DataSourceFactory {
   private static ThreadLocal<DataSource> dataSourceThreadLocal = new ThreadLocal<DataSource>();

   public DataSource getDataSource() {
      if (null == dataSourceThreadLocal.get()) {
         /*
          * allocate
          */
         final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
         try {
            Class.forName(univocityConfiguration.getDbDriver());
         } catch (final Exception e) {
            throw new IllegalStateException(e);
         }
         final SingleConnectionDataSource ds = new SingleConnectionDataSource();
         ds.setUrl(univocityConfiguration.getDbUrl());
         ds.setUsername(univocityConfiguration.getDbUsername());
         ds.setPassword(univocityConfiguration.getDbPassword());
         ds.setSuppressClose(true);
         /*
          * set
          */
         dataSourceThreadLocal.set(ds);
      }
      return dataSourceThreadLocal.get();
   }
}
