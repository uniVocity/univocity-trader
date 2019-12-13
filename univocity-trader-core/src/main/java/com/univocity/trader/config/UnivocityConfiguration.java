package com.univocity.trader.config;

/**
 * @author tom@khubla.com
 */
public interface UnivocityConfiguration {
   String getDbDriver();

   String getDbPassword();

   String getDbUrl();

   String getDbUsername();

   String getExchangeAPIKey();

   String getExchangeAPISecret();

   String getExchangeClass();

   String getExchangeClientId();

   String getMailPassword();

   int getMailPort();

   String getMailReplyto();

   String getMailSender();

   String getMailSMTPphost();

   String getMailUsername();

   boolean isMailSSL();
}