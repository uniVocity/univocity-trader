package com.univocity.trader.factory;

import java.util.function.Supplier;

import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.config.impl.ConfigFileUnivocityConfigurationImpl;
import com.univocity.trader.datasource.DataSourceFactory;
import com.univocity.trader.datasource.impl.ThreadLocalDataSourceFactoryImpl;
import com.univocity.trader.email.MailSenderConfig;
import com.univocity.trader.exchange.Exchange;
import com.univocity.trader.strategy.Strategy;
import com.univocity.trader.strategy.StrategyMonitor;

/**
 * @author tom@khubla.com
 */
public class UnivocityFactory {
   private static UnivocityFactory instance = null;

   public static UnivocityFactory getInstance() {
      if (null == instance) {
         instance = new UnivocityFactory();
      }
      return instance;
   }

   private UnivocityFactory() {
   }

   public DataSourceFactory getDataSourceFactory() {
      return new ThreadLocalDataSourceFactoryImpl();
   }

   public MailSenderConfig getEmailConfig() {
      final UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
      final MailSenderConfig out = new MailSenderConfig();
      out.setReplyToAddress(univocityConfiguration.getMailReplyto());
      out.setSmtpHost(univocityConfiguration.getMailSMTPphost());
      out.setSmtpTlsSsl(univocityConfiguration.isMailSSL());
      out.setSmtpPort(univocityConfiguration.getMailPort());
      out.setSmtpUsername(univocityConfiguration.getMailUsername());
      out.setSmtpPassword(univocityConfiguration.getMailPassword().toCharArray());
      out.setSmtpSender(univocityConfiguration.getMailSender());
      return out;
   }

   public <T> Exchange<T> getExchange(Class<?> clazz) {
      if (null != clazz) {
         try {
            return (Exchange<T>) clazz.getDeclaredConstructor().newInstance();
         } catch (final Exception e) {
            return null;
         }
      }
      return null;
   }

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

   public UnivocityConfiguration getUnivocityConfiguration() {
      return new ConfigFileUnivocityConfigurationImpl();
   }
}
