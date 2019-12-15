package com.univocity.trader.config;

import java.time.LocalDateTime;

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

   Class<?> getExchangeClass();

   String getExchangeClientId();

   String[] getExchangeCurrencies();

   int getExchangeQueryRate();

   String getExchangeReferenceCurrency();

   String getMailPassword();

   int getMailPort();

   String getMailReplyto();

   String getMailSender();

   String getMailSMTPphost();

   String getMailUsername();

   LocalDateTime getSimulationEnd();

   String getSimulationReferenceCurrency();

   LocalDateTime getSimulationStart();

   Class<?> getStrategyClass();

   Class<?>[] getStrategyMonitorClasses();

   boolean isMailSSL();
}