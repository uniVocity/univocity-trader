package com.univocity.trader.datasource;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.univocity.trader.utils.UnivocityConfiguration;

/**
 * @author tom@khubla.com
 */
public class ThreadLocalDataSourceFactory {
   private static ThreadLocal<ThreadLocalDataSourceFactory> instance;

   public static ThreadLocalDataSourceFactory getInstance() {
      if (null == instance) {
         instance = new ThreadLocal<ThreadLocalDataSourceFactory>();
         instance.set(new ThreadLocalDataSourceFactory());
      }
      return instance.get();
   }

   public DataSource getDataSource() {
      final UnivocityConfiguration univocityConfiguration = UnivocityConfiguration.getInstance();
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
      return ds;
   }
}
