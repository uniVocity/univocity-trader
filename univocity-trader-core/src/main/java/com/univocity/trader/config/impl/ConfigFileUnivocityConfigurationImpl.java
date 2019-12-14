package com.univocity.trader.config.impl;

import java.io.FileInputStream;
import java.util.Properties;

import com.univocity.trader.config.UnivocityConfiguration;

/**
 * @author tom@khubla.com
 */
public class ConfigFileUnivocityConfigurationImpl implements UnivocityConfiguration {
   /**
    * default name of config file
    */
   private static final String CONFIGFILE = "univocity.properties";
   /**
    * name of config file
    */
   private static String configfileName = CONFIGFILE;

   public static String getConfigfileName() {
      return configfileName;
   }

   public static void setConfigfileName(String configfileName) {
      ConfigFileUnivocityConfigurationImpl.configfileName = configfileName;
   }

   private String dbDriver;
   private String dbUrl;
   private String dbUsername;
   private String dbPassword;
   private String mailReplyto;
   private String mailSMTPphost;
   private boolean mailSSL;
   private int mailPort;
   private String mailUsername;
   private String mailPassword;
   private String mailSender;
   private String exchangeAPIKey;
   private String exchangeAPISecret;
   private Class<?> exchangeClass;
   private String exchangeClientId;
   private int exchangeQueryRate;
   private Class<?> strategyClass;
   private Class<?> strategyMonitorClass;

   public Class<?> getStrategyMonitorClass() {
      return strategyMonitorClass;
   }

   public Class<?> getStrategyClass() {
      return strategyClass;
   }

   public int getExchangeQueryRate() {
      return exchangeQueryRate;
   }

   public ConfigFileUnivocityConfigurationImpl() {
      try {
         final Properties properties = new Properties();
         properties.load(new FileInputStream(configfileName));
         /*
          * DB properties
          */
         dbUrl = properties.getProperty("db.url");
         dbUsername = properties.getProperty("db.username");
         dbPassword = properties.getProperty("db.password");
         dbDriver = properties.getProperty("db.driver");
         /*
          * mail properties
          */
         mailReplyto = properties.getProperty("mail.replyto");
         mailSMTPphost = properties.getProperty("mail.smtphost");
         mailSSL = Boolean.parseBoolean(properties.getProperty("mail.ssl"));
         mailPort = Integer.parseInt(properties.getProperty("mail.port"));
         mailUsername = properties.getProperty("mail.username");
         mailPassword = properties.getProperty("mail.password");
         mailSender = properties.getProperty("mail.sender");
         /*
          * exchange
          */
         exchangeAPIKey = properties.getProperty("exchange.apikey");
         exchangeAPISecret = properties.getProperty("exchange.secret");
         exchangeClass = (null != properties.getProperty("exchange.class") ? Class.forName(properties.getProperty("exchange.class")) : null);
         exchangeClientId = properties.getProperty("exchange.clientid");
         exchangeQueryRate = Integer.parseInt(properties.getProperty("exchange.queryrate"));
         /*
          * strategy
          */
         strategyClass = (null != properties.getProperty("strategy.class") ? Class.forName(properties.getProperty("strategy.class")) : null);
         strategyMonitorClass = (null != properties.getProperty("strategymonitor.class")) ? Class.forName(properties.getProperty("strategymonitor.class")) : null;
      } catch (final Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public String getDbDriver() {
      return dbDriver;
   }

   @Override
   public String getDbPassword() {
      return dbPassword;
   }

   @Override
   public String getDbUrl() {
      return dbUrl;
   }

   @Override
   public String getDbUsername() {
      return dbUsername;
   }

   @Override
   public String getExchangeAPIKey() {
      return exchangeAPIKey;
   }

   @Override
   public String getExchangeAPISecret() {
      return exchangeAPISecret;
   }

   @Override
   public Class<?> getExchangeClass() {
      return exchangeClass;
   }

   @Override
   public String getExchangeClientId() {
      return exchangeClientId;
   }

   @Override
   public String getMailPassword() {
      return mailPassword;
   }

   @Override
   public int getMailPort() {
      return mailPort;
   }

   @Override
   public String getMailReplyto() {
      return mailReplyto;
   }

   @Override
   public String getMailSender() {
      return mailSender;
   }

   @Override
   public String getMailSMTPphost() {
      return mailSMTPphost;
   }

   @Override
   public String getMailUsername() {
      return mailUsername;
   }

   @Override
   public boolean isMailSSL() {
      return mailSSL;
   }
}
