package com.univocity.trader.utils;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author tom@khubla.com
 */
public class UnivocityConfiguration {
   private static final String CONFIGFILE = "univocity.properties";
   private static UnivocityConfiguration instance;

   public static UnivocityConfiguration getInstance() {
      if (null == instance) {
         instance = new UnivocityConfiguration();
      }
      return instance;
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

   private UnivocityConfiguration() {
      try {
         final Properties properties = new Properties();
         properties.load(new FileInputStream(CONFIGFILE));
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
      } catch (final Exception e) {
         e.printStackTrace();
      }
   }

   public String getDbDriver() {
      return dbDriver;
   }

   public String getDbPassword() {
      return dbPassword;
   }

   public String getDbUrl() {
      return dbUrl;
   }

   public String getDbUsername() {
      return dbUsername;
   }

   public String getMailPassword() {
      return mailPassword;
   }

   public int getMailPort() {
      return mailPort;
   }

   public String getMailReplyto() {
      return mailReplyto;
   }

   public String getMailSender() {
      return mailSender;
   }

   public String getMailSMTPphost() {
      return mailSMTPphost;
   }

   public String getMailUsername() {
      return mailUsername;
   }

   public boolean isMailSSL() {
      return mailSSL;
   }
}
