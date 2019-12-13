package com.univocity.trader.utils;

import com.univocity.trader.config.UnivocityConfiguration;
import com.univocity.trader.guice.UnivocityFactory;
import com.univocity.trader.notification.MailSenderConfig;

/**
 * @author tom@khubla.com
 */
public class MailUtil {
   public static final MailSenderConfig getEmailConfig() {
      UnivocityConfiguration univocityConfiguration = UnivocityFactory.getInstance().getUnivocityConfiguration();
      MailSenderConfig out = new MailSenderConfig();
      out.setReplyToAddress(univocityConfiguration.getMailReplyto());
      out.setSmtpHost(univocityConfiguration.getMailSMTPphost());
      out.setSmtpTlsSsl(univocityConfiguration.isMailSSL());
      out.setSmtpPort(univocityConfiguration.getMailPort());
      out.setSmtpUsername(univocityConfiguration.getMailUsername());
      out.setSmtpPassword(univocityConfiguration.getMailPassword().toCharArray());
      out.setSmtpSender(univocityConfiguration.getMailSender());
      return out;
   }
}
